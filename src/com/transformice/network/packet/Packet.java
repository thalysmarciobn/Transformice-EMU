package com.transformice.network.packet;

import com.transformice.server.helpers.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public interface Packet {
    void parse(Users users, ConcurrentHashMap player, ByteArray _packet, int packetID);
}
