package com.transformice.network.events.room.sync;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._4.C, CC = Identifiers.recv._4.object_sync)
public class ObjectSync implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        int codePartie = packet.readInt();
        ConcurrentHashMap room = (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName));
        if (codePartie == (Integer) room.get(Identifiers.rooms.lastCodePartie) && (Boolean) player.get(Identifiers.player.isSync) && users.server.rooms.getPlayerCount(room) >= 2) {
            ByteArray packet2 = new ByteArray();
            while (packet.bytesAvailable()) {
                packet2.writeShort(packet.readShort());
                short code = packet.readShort();
                byte[] object = new byte[0];
                if (code != -1) {
                    object = new byte[14];
                    packet.read(object);
                }
                packet2.writeShort(code).writeBytes(object);
                if (code != -1) {
                    packet2.writeBoolean(true);
                }
            }
            if (((Long) room.get(Identifiers.rooms.gameStartTimeMillis) + ((Integer) room.get(Identifiers.rooms.roundTime) * 1000)) - System.currentTimeMillis() > 0) {
                users.server.rooms.sendAllOthers(player, (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName)), Identifiers.send.room.sync, packet2.toByteArray());
            }
        }
     }
}
