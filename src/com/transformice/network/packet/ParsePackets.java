package com.transformice.network.packet;

import com.transformice.server.helpers.Users;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class ParsePackets {
    private Users users;

    public ParsePackets(Users users) {
        this.users = users;
    }

    public void parsePacket(Channel channel, ByteArray packet, int packetID) {
        byte[] token = {packet.readByte(), packet.readByte()};
        ConcurrentHashMap player = (ConcurrentHashMap) channel.getAttachment();
        if (Identifiers.recv.screen.version[0] == token[0] && Identifiers.recv.screen.version[1] == token[1]) {
            this.users.sendPacket(channel, Identifiers.send.screen.version, new ByteArray().writeInt(this.users.players.size()).writeByte((Integer) player.get(Identifiers.player.LastPacket)).writeUTF(player.get(Identifiers.player.Langue).toString().toLowerCase()).writeUTF(player.get(Identifiers.player.Langue).toString().toLowerCase()).writeInt((Integer) player.get(Identifiers.player.AuthKey)).toByteArray());
            this.users.sendPacket(channel, Identifiers.send.screen.banner, 52, 0);
            this.users.sendPacket(channel, Identifiers.send.screen.image, new ByteArray().writeUTF("x_noel2014.jpg").toByteArray());
        } else {
            this.users.parsePackets(player, token, packet, packetID);
        }
    }
}
