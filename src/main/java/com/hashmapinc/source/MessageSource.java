package com.hashmapinc.source;

import akka.actor.ActorRef;
import akka.stream.Attributes;
import akka.stream.Outlet;
import akka.stream.SourceShape;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;
import akka.util.ByteString;
import com.hashmapinc.messages.AssignStageActor;
import com.hashmapinc.messages.RemoveStageActor;
import com.hashmapinc.messages.StreamCompleted;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class MessageSource extends GraphStage<SourceShape<ByteString>> {

    public final Outlet<ByteString> out = Outlet.create("MessageSource.out");
    private final SourceShape<ByteString> shape = SourceShape.of(out);
    private final ActorRef sourceActor;


    public MessageSource(ActorRef sourceActor){
        this.sourceActor = sourceActor;
    }

    @Override
    public GraphStageLogic createLogic(Attributes inheritedAttributes) throws Exception {
        return new GraphStageLogic(shape()){
            private final String stageActorId = UUID.randomUUID().toString();
            private final Queue<ByteString> messages = new LinkedList<>();

            private StageActor self;

            private AbstractFunction1<Tuple2<ActorRef, Object>, BoxedUnit> onMessage() {
                return new AbstractFunction1<Tuple2<ActorRef, Object>, BoxedUnit>() {
                    @Override
                    public BoxedUnit apply(Tuple2<ActorRef, Object> v1) {
                        if(v1._2 instanceof ByteString){
                            messages.add((ByteString)v1._2);
                            pump();
                        }else if(v1._2 instanceof StreamCompleted){
                            //Need to find way to terminate this graph stage
                        }
                        return BoxedUnit.UNIT;
                    }
                };
            }

            {
                setHandler(out, new AbstractOutHandler() {
                    @Override
                    public void onPull() throws Exception {
                        pump();
                    }
                });
            }

            @Override
            public String stageActorName() {
                return stageActorId;
            }

            @Override
            public void preStart() throws Exception {
                self = getStageActor(onMessage());
                sourceActor.tell(new AssignStageActor(stageActorName(), self.ref()), ActorRef.noSender());
            }

            @Override
            public void postStop() throws Exception {
                sourceActor.tell(new RemoveStageActor(stageActorName()), ActorRef.noSender());
            }

            private void pump(){
                if(isAvailable(out) && !messages.isEmpty()){
                    push(out, messages.poll());
                }
            }
        };
    }

    @Override
    public SourceShape<ByteString> shape() {
        return shape;
    }

}
