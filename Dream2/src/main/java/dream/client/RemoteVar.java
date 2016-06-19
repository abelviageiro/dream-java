package dream.client;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import dream.common.Consts;
import dream.common.SerializablePredicate;
import dream.common.packets.EventPacket;
import dream.common.packets.content.Subscription;

public class RemoteVar<T extends Serializable> implements Subscriber, UpdateProducer<T> {
	private T val;

	private final ClientEventForwarder forwarder;
	private final Map<UpdateConsumer, List<SerializablePredicate<T>>> consumers = new HashMap<>();

	private final Queue<EventPacket> eventsQueue = new ArrayDeque<>();
	private int pendingAcks = 0;

	private final List<SerializablePredicate<T>> constraints = new ArrayList<>();

	private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private final String host;
	private final String object;

	public RemoteVar(String host, String object, List<SerializablePredicate<T>> constraints) {
		this.host = host;
		this.object = object;

		final Subscription<T> sub = new Subscription<T>(host, object, constraints);
		forwarder = ClientEventForwarder.get();
		forwarder.addSubscription(this, sub);
	}

	public RemoteVar(String object, List<SerializablePredicate<T>> constraints) {
		this(Consts.hostName, object, constraints);
	}

	public RemoteVar(String host, String object) {
		this(host, object, new ArrayList<SerializablePredicate<T>>());
	}

	public RemoteVar(String object) {
		this(Consts.hostName, object);
	}

	public final synchronized T get() {
		return val;
	}

	@Override
	public synchronized void notifyEventReceived(EventPacket evPkt) {
		eventsQueue.add(evPkt);
		logger.finest("Received event packet " + evPkt + ". Added to the queue1.");
		if (eventsQueue.size() == 1) {
			logger.finest("The element is the only one in the queue1. Let's process it.");
			processNextEvent();
		}
	}

	@Override
	public final synchronized void notifyUpdateFinished() {
		pendingAcks--;
		processNextEvent();
	}

	@SuppressWarnings("unchecked")
	private void processNextEvent() {
		if (pendingAcks == 0 && !eventsQueue.isEmpty()) {
			final EventPacket nextPkt = eventsQueue.poll();
			val = (T) nextPkt.getEvent().getVal();
			sendEventPacketToListeners(nextPkt);
		}
	}

	private final void sendEventPacketToListeners(EventPacket evPkt) {
		if (!consumers.isEmpty()) {
			final Set<UpdateConsumer> satConsumers = //
			consumers.entrySet().stream().filter(e -> e.getValue().stream().allMatch(constr -> constr.test(val)))//
					.map(e -> e.getKey())//
					.collect(Collectors.toSet());

			pendingAcks = satConsumers.size();
			satConsumers.forEach(c -> c.updateFromProducer(evPkt, this));
		} else {
			processNextEvent();
		}
	}

	@Override
	public final String getHost() {
		return host;
	}

	@Override
	public final String getObject() {
		return object;
	}

	@Override
	public final List<SerializablePredicate<T>> getConstraints() {
		return constraints;
	}

	@Override
	public final void registerUpdateConsumer(UpdateConsumer consumer, List<SerializablePredicate<T>> constraints) {
		consumers.put(consumer, constraints);
	}

	@Override
	public final void unregisterUpdateConsumer(UpdateConsumer consumer) {
		consumers.remove(consumer);
	}

	@Override
	public UpdateProducer<T> filter(SerializablePredicate<T> constraint) {
		final List<SerializablePredicate<T>> constrList = new ArrayList<>();
		constrList.add(constraint);
		return new FilteredUpdateProducer<>(this, constrList);
	}

}
