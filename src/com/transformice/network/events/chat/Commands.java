package com.transformice.network.events.chat;

import com.transformice.network.packet.*;
import com.transformice.server.config.Config;
import com.transformice.server.users.Users;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._6.C, CC = Identifiers.recv._6.commands)
public class Commands implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        packet = users.packetManage.decrypt(packetID, packet, Config.packetKeys);
        String command = packet.readUTF();
        try {
            users.commands.parse(player, command);
        } catch (PacketException message) {
            users.sendMessage((Channel) player.get(Identifiers.player.Channel), message.getMessage());
        }
    }
}
