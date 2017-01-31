package com.transformice.network.events.room.sync;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._5.C, CC = Identifiers.recv._5.bridge_break)
public class BridgeBreak implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        ConcurrentHashMap room = (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName));
        if (ArrayUtils.contains(users.server.rooms.bridgeBreakMaps, (Integer) room.get(Identifiers.rooms.currentMap))) {
            int bridgeCode = packet.readShort();
            users.server.rooms.sendAllOthers(player, room, Identifiers.send.room.bridge_break, new ByteArray().writeShort(bridgeCode).toByteArray());
        }
    }
}
