package com.hashmapinc.tcp;

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

public class TcpService implements ServiceApi {

    private static ActorSystem system = ActorSystem.create("TCP System");
    private ActorRef tcpServer;

    public void init() throws Exception {
        this.tcpServer = system.actorOf(Props.create(TcpServerActor.class, new TcpServerActor.ActorCreator().create()));
    }

    @Override
    public Source<ByteString, NotUsed> buildSource(SourceConfig conf) throws Exception{
        //This will create once server per call, to avoid this instantiate one actor at class level
        //Don't store channel in TcpServerActor but pass it to handler directly as for each connection
        //Have a list of channels in Handler so that multiple Sinks can subscribe to it

        //Conf can contain details of server
        //ActorRef tcpServer = system.actorOf(Props.create(TcpServerActor.class, new TcpServerActor.ActorCreator().create()));

        MessageSource source = new MessageSource(this.tcpServer);
        Source<ByteString, NotUsed> tcpSource = Source.fromGraph(source);

        return tcpSource.via(
                //Just for example, we need to have our own Flow which converts ByteString to Class object and then
                // serialize to ByteString. Look at JsonFraming.objectScanner for reference.
                Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW)
        );
    }
}
