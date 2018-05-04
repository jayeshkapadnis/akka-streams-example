package com.hashmapinc.tcp.server;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import com.hashmapinc.messages.AssignChannels;
import com.hashmapinc.messages.AssignStageActor;
import com.hashmapinc.messages.RemoveHandler;
import com.hashmapinc.messages.RemoveStageActor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TcpServerActor extends AbstractLoggingActor{

    private ActorRef tcpActor;
    private Map<String, ActorRef> channels;
    private Map<UUID, ActorRef> handlers;

    public TcpServerActor(){
        channels = new HashMap<>();
        handlers = new HashMap<>();
    }

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
                    ActorRef sender = getSender();
                    UUID id = UUID.randomUUID();
                    final ActorRef handler = getContext().actorOf(
                            Props.create(TcpMessageHandler.class), id.toString());
                    handlers.put(id, handler);
                    handler.tell(new AssignChannels(channels), self());
                    sender.tell(TcpMessage.register(handler), getSelf());
                }).match(AssignStageActor.class, a ->{
                    channels.putIfAbsent(a.getName(), a.getStageActor());
                }).match(RemoveStageActor.class, r -> {
                    channels.remove(r.getName());
                    //notify all handlers about channel removal
                    handlers.forEach((id, c) -> c.tell(r, self()));
                }).match(RemoveHandler.class, h -> {
                    handlers.remove(h.getName());
                }).matchAny(o -> log().warning("Unhandled message: [{}]", o))
                .build();
    }
}
