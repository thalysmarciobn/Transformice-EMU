package com.transformice.network.packet;

public class PacketException extends Exception {
    private String type = "warning";

    public PacketException(String msg) {
        super(msg);
    }

    public PacketException(String msg, String type) {
        super(msg);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
