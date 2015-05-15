package javareact.common.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javareact.common.packets.content.Event;
import javareact.server.WaitRecommendations;
import protopeer.network.Message;
import protopeer.util.quantities.Data;

/**
 * Packet used to deliver events, which notify about some state change.
 */
public class EventPacket extends Message {
  private static final long serialVersionUID = 8208653909787190211L;
  public static final String subject = "__JAVA_REACT_PUBLICATION_PACKET_SUBJECT";

  private final Event event;
  private final UUID id;
  private final Set<String> computedFrom = new HashSet<String>();
  private final double creationTime;
  private boolean approvedByTokenService = false;

  // Wait recommendations are used in glitch free protocols to tell the client to wait before processing an event
  private final Map<String, Set<WaitRecommendations>> waitRecommendations = new HashMap<String, Set<WaitRecommendations>>();

  // Final expressions are used in the atomic protocol to determine when the token can be released
  // Upon receiving a final expression, the server acknowledges the token manager
  private final Set<String> finalExpressions = new HashSet<String>();

  public EventPacket(Event event, UUID id, Set<String> computedFrom, double creationTime, boolean approvedByTokenService) {
    this.event = event;
    this.id = id;
    this.computedFrom.addAll(computedFrom);
    this.creationTime = creationTime;
    this.approvedByTokenService = approvedByTokenService;
  }

  public EventPacket(Event event, UUID id, double creationTime, boolean approvedByTokenService) {
    this.event = event;
    this.id = id;
    this.creationTime = creationTime;
    this.approvedByTokenService = approvedByTokenService;
  }

  public final Event getEvent() {
    return event;
  }

  public final UUID getId() {
    return id;
  }

  public final Set<String> getComputedFrom() {
    return computedFrom;
  }

  public final void addWaitRecommendations(WaitRecommendations recommendations) {
    String expression = recommendations.getExpression();
    Set<WaitRecommendations> innerSet = waitRecommendations.get(expression);
    if (innerSet == null) {
      innerSet = new HashSet<WaitRecommendations>();
      waitRecommendations.put(expression, innerSet);
    }
    innerSet.add(recommendations);
  }

  public final boolean hasRecommendationsFor(String expression) {
    return waitRecommendations.containsKey(expression);
  }

  public final Set<WaitRecommendations> getRecommendationsFor(String expression) {
    return waitRecommendations.get(expression);
  }

  public final void addFinalExpression(String expression) {
    finalExpressions.add(expression);
  }

  public final Set<String> getFinalExpressions() {
    return finalExpressions;
  }

  public final boolean isFinal() {
    return finalExpressions.contains(event.getSignature());
  }

  public final EventPacket dup() {
    EventPacket result = new EventPacket(event, id, creationTime, approvedByTokenService);
    result.computedFrom.addAll(computedFrom);
    result.finalExpressions.addAll(finalExpressions);
    result.waitRecommendations.putAll(waitRecommendations);
    return result;
  }

  public final double getCreationTime() {
    return creationTime;
  }

  public final void tokenServiceApproves() {
    approvedByTokenService = true;
  }

  public final boolean isApprovedByTokenService() {
    return approvedByTokenService;
  }

  @Override
  public String toString() {
    return "EventPacket [event=" + event + ", id=" + id + ", computedFrom=" + computedFrom + ", waitRecommendations=" + waitRecommendations + ", finalExpressions=" + finalExpressions + "]";
  }

  @Override
  public Data getSize() {
    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream;
    try {
      objectOutputStream = new ObjectOutputStream(byteOutputStream);
      objectOutputStream.writeObject(this);
      objectOutputStream.flush();
      objectOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Data.inByte(byteOutputStream.toByteArray().length);
  }

}
