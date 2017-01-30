package com.transformice.network.events.old;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Arrays;

@PacketEvent(C = Identifiers.recv._1.C, CC = Identifiers.recv._1.Old_Protocol)
public class OldProtocol implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        String _packet = packet.readUTF();
        String[] values = _packet.split(String.valueOf('\u0001'));
        int C = (int) (values[0].charAt(0));
        int CC = (int) (values[0].charAt(1));
        values = Arrays.copyOfRange(values, 1, values.length);
        ConcurrentHashMap room = (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName));
        if (C == Identifiers.recv.old.dummy.C) {
            if (CC == Identifiers.recv.old.dummy.dummy) {
                player.replace(Identifiers.player.isReceivedDummy, true);
            }
        } else if (C == Identifiers.recv.old.anchors.C) {
            if (CC == Identifiers.recv.old.anchors.anchors) {
                users.server.rooms.sendAllOld(room, Identifiers.send.old.room.anchors, (Object[]) values);
                room.replace(Identifiers.rooms.Anchors, ArrayUtils.addAll((String[]) room.get(Identifiers.rooms.Anchors), values));
            }
        }
    }
}
