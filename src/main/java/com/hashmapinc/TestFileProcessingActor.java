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
import com.hashmapinc.file.FileProcessingActor;
import com.hashmapinc.source.MessageSource;

public class TestFileProcessingActor {

    public static void main(String[] args) throws Exception {
        String filePath = "/path/to/csv";
        ActorSystem system = ActorSystem.create("File Stream Test");
        Materializer mat = ActorMaterializer.create(system);

        ActorRef fileReader = system.actorOf(Props.create(FileProcessingActor.class, new FileProcessingActor.ActorCreator(filePath).create()));

        MessageSource source = new MessageSource(fileReader);
        Source<ByteString, NotUsed> fileSource = Source.fromGraph(source);

        Source<ByteString, NotUsed> returnSource = fileSource.via(
                Framing.delimiter(ByteString.fromString(System.lineSeparator()), 512, FramingTruncation.ALLOW)
        );

        returnSource.runForeach(System.out::println, mat);
    }
}
