package com.bloxbean.cardano.client.plutus.annotation.processor.model;

public enum Type {
    INTEGER("integer"),
    STRING("string"),
    BYTES("bytes"),
    LIST("list"),
    MAP("map"),
    CONSTRUCTOR("constructor"),
    OPTIONAL("optional");

    private String type;
    Type(String type) {
        this.type = type;
    }
}
