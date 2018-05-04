package com.hashmapinc.tcp.client;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import com.hashmapinc.messages.AssignStageActor;
import com.hashmapinc.messages.RemoveStageActor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class TcpClientActor extends AbstractLoggingActor{
    private final InetSocketAddress remote;
    private ActorRef tcpActor;
    private Map<String, ActorRef> channels;

    public TcpClientActor(InetSocketAddress remote) {
        this.remote = remote;
        this.channels = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }

        tcpActor.tell(TcpMessage.connect(remote), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Tcp.Connected.class, c -> {
            ActorRef sender = getSender();
            sender.tell(TcpMessage.register(getSelf()), getSelf());
            getContext().become(connected(sender));
        }).build();
    }

    private Receive connected(final ActorRef connection){
        return receiveBuilder().match(Tcp.Received.class, r -> {
            channels.forEach((id, c) -> c.tell(r.data(), getSelf()));
        }).match(ByteString.class, b -> {
            connection.tell(TcpMessage.write(b), getSelf());
        }).match(Tcp.CommandFailed.class, f -> {
            log().info("Command Failed: [{}]", f);
            getContext().stop(self());
        }).match(Tcp.ConnectionClosed.class, c->{
            getContext().stop(getSelf());
        }).match(AssignStageActor.class, a ->{
            channels.putIfAbsent(a.getName(), a.getStageActor());
        }).match(RemoveStageActor.class, r -> {
            channels.remove(r.getName());
        }).build();
    }
}
