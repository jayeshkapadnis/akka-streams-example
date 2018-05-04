package com.hashmapinc.tcp.client;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.hashmapinc.api.ServiceApi;
import com.hashmapinc.api.SourceConfig;
import com.hashmapinc.source.MessageSource;

import java.net.InetSocketAddress;

public class TcpClientService implements ServiceApi{
    private static ActorSystem system = ActorSystem.create("TCP-CLIENT");

    @Override
    public Source<ByteString, NotUsed> buildSource(SourceConfig conf) throws Exception {
        String host = conf.getProperty("tcp.host");
        int port = Integer.parseInt(conf.getProperty("tcp.port"));
        ActorRef tcpClient = system.actorOf(Props.create(TcpClientActor.class, new InetSocketAddress(host, port)));

        MessageSource source = new MessageSource(tcpClient);
        Source<ByteString, NotUsed> tcpSource = Source.fromGraph(source);

        return tcpSource.via(
                //Just for example, we need to have our own Flow which converts ByteString to Class object and then
                // serialize to ByteString. Look at JsonFraming.objectScanner for reference.
                Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW)
        );
    }
}
