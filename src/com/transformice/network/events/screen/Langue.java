package com.transformice.network.events.screen;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._8.C, CC = Identifiers.recv._8.Langue)
public class Langue implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        int langueID = packet.readByte();
        String langue = users.server.langues.getLangue(langueID);
        player.replace(Identifiers.player.langueByte, langueID);
        player.replace(Identifiers.player.Langue, langue);
    }
}
