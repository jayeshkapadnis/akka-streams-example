package com.hashmapinc.tcp.server;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.io.Tcp;
import akka.util.ByteString;
import com.hashmapinc.messages.AssignChannels;
import com.hashmapinc.messages.AssignStageActor;
import com.hashmapinc.messages.RemoveHandler;
import com.hashmapinc.messages.RemoveStageActor;

import java.util.Map;
import java.util.UUID;

public class TcpMessageHandler extends AbstractLoggingActor{

    private Map<String, ActorRef> channels;
    private ActorRef server;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(AssignChannels.class, r -> {
            server = getSender();
            channels = r.getChannels();
            getContext().become(active());
        }).matchAny(m -> {
            log().error("Invalid message received: [{}]", m);
        }).build();
    }

    private Receive active(){
        return receiveBuilder().match(Tcp.Received.class, m -> {
            ByteString msg = m.data();
            channels.forEach((id, c) -> c.tell(msg, self()));
        }).match(RemoveStageActor.class, r -> {
            channels.remove(r.getName());
        }).match(Tcp.ConnectionClosed.class, msg -> {
            server.tell(new RemoveHandler(UUID.fromString(getSelf().path().name())), ActorRef.noSender());
            getContext().stop(getSelf());
        }).build();
    }
}
