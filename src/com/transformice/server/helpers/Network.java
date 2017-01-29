package com.transformice.server.helpers;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Map;

public class Network {
    private final Map<Integer, byte[]> incompletePackets = new ConcurrentHashMap();

    public boolean checkIncompletePacket(Channel channel) {
        return this.incompletePackets.containsKey(channel.getId());
    }

    public byte[] getIncompletePacket(Channel channel) {
        byte[] buff = this.incompletePackets.get(channel.getId());
        this.incompletePackets.remove(channel.getId());
        return buff;
    }

    public void putIncompletePacket(Channel channel, byte[] buff) {
        this.incompletePackets.put(channel.getId(), buff);
    }
}
