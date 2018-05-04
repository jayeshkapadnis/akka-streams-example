package com.hashmapinc.tcp;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.Tcp.*;
import akka.io.TcpMessage;
import akka.japi.Creator;
import com.hashmapinc.messages.AssignStageActor;

import java.net.InetSocketAddress;

public class TcpServerActor extends AbstractLoggingActor{

    private ActorRef tcpActor;
    private ActorRef channel;

    @Override
    public void preStart() throws Exception {
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }

        tcpActor.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 9090), 100), getSelf());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Bound.class, b ->{
                    log().info("Received Bound message: [{}]", b);
                }).match(CommandFailed.class, f -> {
                    log().info("Command Failed: [{}]", f);
                    getContext().stop(self());
                }).match(Connected.class, c -> {
                    log().info("Received Connected message: [{}]", c);
                    final ActorRef handler = getContext().actorOf(
                            Props.create(TcpMessageHandler.class));
                    handler.tell(new AssignStageActor(channel), self());
                    getSender().tell(TcpMessage.register(handler), getSelf());
                }).match(AssignStageActor.class, a ->{
                    channel = a.getStageActor();
                }).matchAny(o -> log().warning("Unhandled message: [{}]", o))
                .build();
    }

    public static class ActorCreator implements Creator<TcpServerActor>{
        private static final long serialVersionUID = 1L;

        @Override
        public TcpServerActor create() throws Exception {
            return new TcpServerActor();
        }
    }
}
