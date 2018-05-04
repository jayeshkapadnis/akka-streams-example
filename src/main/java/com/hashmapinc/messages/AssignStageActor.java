package com.hashmapinc.messages;

import akka.actor.ActorRef;

import java.io.Serializable;

public class AssignStageActor implements Serializable{

    private final ActorRef stageActor;
    private final String name;

    public AssignStageActor(String name, ActorRef stageActor) {
        this.name = name;
        this.stageActor = stageActor;
    }

    public ActorRef getStageActor() {
        return stageActor;
    }

    public String getName() {
        return name;
    }
}
