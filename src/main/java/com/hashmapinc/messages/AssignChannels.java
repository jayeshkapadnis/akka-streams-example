package com.hashmapinc.messages;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Map;

public class AssignChannels implements Serializable{

    private final Map<String, ActorRef> channels;

    public AssignChannels(Map<String, ActorRef> channels) {
        this.channels = channels;
    }

    public Map<String, ActorRef> getChannels() {
        return channels;
    }
}
