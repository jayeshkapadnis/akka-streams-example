package com.hashmapinc.file;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.Creator;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import com.hashmapinc.messages.AssignStageActor;
import com.hashmapinc.messages.StreamCompleted;

import java.io.File;
import java.util.concurrent.CompletionStage;

public class FileProcessingActor extends AbstractLoggingActor{

    private final String filePath;
    private Materializer mat;

    public FileProcessingActor(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void preStart() throws Exception {
        this.mat = ActorMaterializer.create(getContext().getSystem());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(AssignStageActor.class, a -> {
            readFileStream(a.getStageActor());
        }).build(); //Add match any
    }

    private void readFileStream(ActorRef channel){
        CompletionStage<IOResult> run = FileIO.fromFile(new File(filePath))
                .to(Sink.actorRef(channel, new StreamCompleted())).run(mat);
        run.whenCompleteAsync((a, b) ->{
            if(a.wasSuccessful()){
                log().info("Operation was successful");
            }else {
                Throwable error = a.getError();
                if(b != null || error != null){
                    log().error("Error occurred while file processing", error);
                }
            }
        });
    }

    public static class ActorCreator implements Creator<FileProcessingActor> {
        private static final long serialVersionUID = 1L;
        private final String path;

        public ActorCreator(String path){
            this.path = path;
        }
        @Override
        public FileProcessingActor create() throws Exception {
            return new FileProcessingActor(path);
        }
    }
}
