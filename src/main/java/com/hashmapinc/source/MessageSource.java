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
import com.hashmapinc.messages.StreamCompleted;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.util.LinkedList;
import java.util.Queue;

public class MessageSource extends GraphStage<SourceShape<ByteString>> {

    public final Outlet<ByteString> out = Outlet.create("MessageSource.out");
    private final SourceShape<ByteString> shape = SourceShape.of(out);
    private final ActorRef sourceActor;
    private final Queue<ByteString> messages;


    public MessageSource(ActorRef sourceActor){
        this.sourceActor = sourceActor;
        this.messages = new LinkedList<>();
    }

    @Override
    public GraphStageLogic createLogic(Attributes inheritedAttributes) throws Exception {
        new GraphStageLogic(shape()){
            private StageActor self = getStageActor(new AbstractFunction1<Tuple2<ActorRef, Object>, BoxedUnit>() {
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
            });

            {
                setHandler(out, new AbstractOutHandler() {
                    @Override
                    public void onPull() throws Exception {
                        pump();
                    }
                });
            }

            @Override
            public void preStart() throws Exception {
                sourceActor.tell(new AssignStageActor(self.ref()), ActorRef.noSender());
            }

            private void pump(){
                if(isAvailable(out) && !messages.isEmpty()){
                    push(out, messages.poll());
                }
            }
        };

        return null;
    }

    @Override
    public SourceShape<ByteString> shape() {
        return shape;
    }

}
