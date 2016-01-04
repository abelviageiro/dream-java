package javareact.common.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javareact.client.ClientEventForwarder;
import javareact.client.QueueManager;
import javareact.common.Consts;
import javareact.common.ValueChangeListener;
import javareact.common.packets.EventPacket;
import javareact.common.packets.content.Advertisement;
import javareact.common.packets.content.Attribute;
import javareact.common.packets.content.Constraint;
import javareact.common.packets.content.Event;
import javareact.common.packets.content.Subscription;

public class Signal<T> implements TimeChangingValue<T>, ProxyGenerator, ProxyChangeListener {
  private final Set<ValueChangeListener<T>> listeners = new HashSet<ValueChangeListener<T>>();
  private final ClientEventForwarder clientEventForwarder;
  private final QueueManager queueManager = new QueueManager();
  private final String objectId;
  private final Supplier<T> evaluation;
  private final List<Proxy> dependentProxies = new ArrayList<Proxy>();

  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  private RemoteVar<T> proxy = null;
  protected T val;

  public Signal(String name, Supplier<T> evaluation, ProxyGenerator... vars) {
    this.objectId = name;
    clientEventForwarder = ClientEventForwarder.get();
    for (final ProxyGenerator var : vars) {
      final Proxy varProxy = var.getProxy();
      dependentProxies.add(varProxy);
      varProxy.addProxyChangeListener(this);
    }
    this.evaluation = evaluation;
    sendAdvertisement();
  }

  @Override
  public void update(EventProxyPair eventProxyPair) {
    logger.finest("Update method invoked with " + eventProxyPair);
    final List<EventProxyPair> pairs = queueManager.processEventPacket(eventProxyPair, objectId + "@" + Consts.hostName);
    logger.finest("The queueManager returned the following pairs " + pairs);
    if (!pairs.isEmpty()) {
      logger.finest("Actual update");
      // Compute the new value
      try {
        val = evaluate();
        logger.finest("New value computed for the reactive object: " + val);
      } catch (final Exception e) {
        logger.info("Exception during the evaluation of the expression.");
        return;
      }

      // Notify dependent objects
      final EventProxyPair templatePair = pairs.get(0);
      final EventPacket templatePkt = templatePair.getEventPacket();
      final UUID id = templatePkt.getId();
      final String initialVar = getInitialVar(pairs);
      final Set<String> finalExpressions = templatePkt.getFinalExpressions();
      Event ev = null;
      try {
        // TODO consider methods other than get()!!!
        ev = new Event(Consts.hostName, objectId, Attribute.of("get", val));
      } catch (final Exception e) {
        e.printStackTrace();
      }
      logger.finest("Sending event to dependent reactive objects.");
      clientEventForwarder.sendEvent(id, ev, initialVar, finalExpressions, true);

      // Notify listeners
      logger.finest("Notifying registered listeners of the change.");
      listeners.forEach(l -> l.notifyValueChanged(val));

      // Acknowledge the proxies
      logger.finest("Acknowledging the proxies.");
      pairs.forEach(pair -> pair.getProxy().notifyEventProcessed(this, pair.getEventPacket()));
    } else {
      logger.finest(objectId + ": update call but waiting: " + eventProxyPair.toString());
    }
  }

  @Override
  public void addValueChangeListener(ValueChangeListener<T> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeValueChangeListener(ValueChangeListener<T> listener) {
    listeners.remove(listener);
  }

  private final void sendAdvertisement() {
    final Set<Subscription> subs = dependentProxies.stream().//
        map(p -> new Subscription(p.getHost(), p.getObject(), p.getProxyID(), new Constraint(p.getMethod()))).//
        collect(Collectors.toSet());
    clientEventForwarder.advertise(new Advertisement(Consts.hostName, objectId), subs, true);
  }

  private final String getInitialVar(Collection<EventProxyPair> pairs) {
    // All pairs are generated from the same initial var, so we can retrieve the
    // initial var from any event
    return pairs.stream().//
        findAny().//
        map(p -> p.getEventPacket().getInitialVar()).//
        get();
  }

  @Override
  public final synchronized T evaluate() {
    return evaluation.get();
  }

  public T get() {
    return val;
  }

  @Override
  public synchronized RemoteVar<T> getProxy() {
    if (proxy == null) {
      proxy = new RemoteVar<T>(objectId);
    }
    return proxy;
  }

}
