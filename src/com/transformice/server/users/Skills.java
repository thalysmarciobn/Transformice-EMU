package com.transformice.server.users;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.server.rooms.Rooms;
import com.transformice.server.users.Users;
import org.jboss.netty.channel.Channel;

public class Skills {
    private Users users;
    private Rooms rooms;

    public Skills(Users users, Rooms rooms) {
        this.users = users;
        this.rooms = rooms;
    }

    public void sendExp(Channel channel, int level, int exp, int nextLevel) {
        this.users.sendPacket(channel, Identifiers.send.player.shaman_exp, new ByteArray().writeShort(level - 1).writeInt(exp).writeInt(nextLevel).toByteArray());
    }

    public void sendShamanSkills(Channel channel, boolean type) {
        ByteArray packet = new ByteArray().writeByte(0); // size
        this.users.sendPacket(channel, Identifiers.send.player.shaman_skills, packet.writeBoolean(type).toByteArray());
    }
}
