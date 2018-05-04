package com.hashmapinc.file;

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

public class FileService implements ServiceApi {
    private final ActorSystem system = ActorSystem.create("File Stream Test");

    @Override
    public Source<ByteString, NotUsed> buildSource(SourceConfig conf) throws Exception {
        String filePath = conf.getProperty("file.path");
        ActorRef fileReader = system.actorOf(Props.create(FileProcessingActor.class, filePath));
        MessageSource source = new MessageSource(fileReader);
        Source<ByteString, NotUsed> fileSource = Source.fromGraph(source);
        return fileSource.via(
                Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW)
        );
    }
}
