package com.hashmapinc.messages;

import java.io.Serializable;

public class RemoveStageActor implements Serializable{

    private final String name;

    public RemoveStageActor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
