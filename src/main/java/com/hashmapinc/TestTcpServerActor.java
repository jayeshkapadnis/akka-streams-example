package com.hashmapinc;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.hashmapinc.source.MessageSource;
import com.hashmapinc.tcp.TcpServerActor;

public class TestTcpServerActor {

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("TCP-System");
        Materializer mat = ActorMaterializer.create(system);

        ActorRef tcpServer = system.actorOf(Props.create(TcpServerActor.class));

        MessageSource source = new MessageSource(tcpServer);
        Source<ByteString, NotUsed> tcpSource = Source.fromGraph(source);

        Source<ByteString, NotUsed> returnSource = tcpSource.via(
                //Just for example, we need to have our own Flow which converts ByteString to Class object and then
                // serialize to ByteString. Look at JsonFraming.objectScanner for reference.
                Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW)
        );

        tcpSource.runForeach(System.out::println, mat);
    }
}
