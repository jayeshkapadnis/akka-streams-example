package com.hashmapinc.messages;

import akka.actor.ActorRef;

import java.io.Serializable;

public class AssignStageActor implements Serializable{

    private final ActorRef stageActor;

    public AssignStageActor(ActorRef stageActor) {
        this.stageActor = stageActor;
    }

    public ActorRef getStageActor() {
        return stageActor;
    }
}
