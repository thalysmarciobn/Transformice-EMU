package com.transformice.network.events.room.sync;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._4.C, CC = Identifiers.recv._4.crouch)
public class Crouch implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        users.server.rooms.sendAll((ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName)), Identifiers.send.room.crouch, new ByteArray().writeInt((Integer) player.get(Identifiers.player.Code)).writeByte(packet.readByte()).writeByte(0).toByteArray());
    }
}
