package com.hashmapinc.messages;

import java.io.Serializable;
import java.util.UUID;

public class RemoveHandler implements Serializable{

    private final UUID name;

    public RemoveHandler(UUID name) {
        this.name = name;
    }

    public UUID getName() {
        return name;
    }
}
