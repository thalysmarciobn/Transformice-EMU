package com.transformice.server.helpers;

import com.transformice.network.packet.Identifiers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Sessions {
    private final Map<Integer, byte[]> incompletePackets = new ConcurrentHashMap();

    public void addSession(Channel channel) {
        ConcurrentHashMap player = new ConcurrentHashMap();
        player.put(Identifiers.player.Channel, channel);
        player.put(Identifiers.player.Username, "");
        player.put(Identifiers.player.LastPacket, ThreadLocalRandom.current().nextInt(0,99));
        player.put(Identifiers.player.AuthKey, ThreadLocalRandom.current().nextInt(0,Integer.MAX_VALUE));
        player.put(Identifiers.player.Langue, "BR");
        player.put(Identifiers.player.langueByte, 3);
        channel.setAttachment(player);
    }

    public void removeSession(Channel channel) {
        ConcurrentHashMap player = (ConcurrentHashMap) channel.getAttachment();
        player.clear();
    }

    public void close(Channel channel) {
        channel.close();
    }

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
