package com.hashmapinc.tcp;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.io.Tcp;
import akka.util.ByteString;
import com.hashmapinc.messages.AssignStageActor;

public class TcpMessageHandler extends AbstractLoggingActor{

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(AssignStageActor.class, r -> {
            getContext().become(active(r.getStageActor()));
        }).matchAny(m -> {
            log().error("Invalid message received: [{}]", m);
        }).build();
    }

    private Receive active(final ActorRef channel){
        return receiveBuilder().match(Tcp.Received.class, m -> {
            ByteString msg = m.data();
            channel.tell(msg, self());
        }).match(Tcp.ConnectionClosed.class, msg -> {
            getContext().stop(getSelf());
        }).build();
    }
}
