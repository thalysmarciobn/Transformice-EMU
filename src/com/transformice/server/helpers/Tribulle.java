package com.transformice.server.helpers;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.server.Server;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class Tribulle {
    private Server server;
    private Users users;

    public Tribulle(Server server, Users users) {
        this.server = server;
        this.users = users;
    }

    private void sendPacket(Channel channel, int code, byte[] result) {
        ByteArray packet = new ByteArray();
        packet.writeShort(code);
        packet.write(result);
        this.users.sendPacket(channel, Identifiers.send.server.tribulle, packet.toByteArray());
    }

    public void sendPlayerInfo(ConcurrentHashMap player) {
        ByteArray packet = new ByteArray();
        packet.writeInt(0);
        packet.writeInt((Integer) player.get(Identifiers.player.playerID));
        packet.writeInt((Integer) player.get(Identifiers.player.playerID));
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeUTF("");
        this.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.tribulle.send.ET_ReponseDemandeInfosJeuUtilisateur, packet.toByteArray());
    }

    public void sendFriendList(ConcurrentHashMap player, ByteArray readPacket) {
        int tribulleID = readPacket == null ? 0 : readPacket.readInt();
        ByteArray packet = new ByteArray();
        packet.writeInt(tribulleID);
        ByteArray friends = new ByteArray();
        packet.writeShort(friends.size());
        packet.writeBytes(friends.toByteArray());
        this.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.tribulle.send.ET_ResultatListeAmis, packet.toByteArray());
    }

    public void sendIgnoredsList(ConcurrentHashMap player) {
        ByteArray packet = new ByteArray();
        packet.writeInt(0);
        packet.writeShort(0); // size
        //packet.write(this.getString(playerName, this.PLAYER_NAME_LEN));
        this.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.tribulle.send.ET_ResultatListeNoire, packet.toByteArray());
    }

    public void sendTribe(ConcurrentHashMap player, boolean isNew) {
        ByteArray packet = new ByteArray();
        packet.writeInt(0);
        packet.writeByte(0);
        this.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.tribulle.send.ET_ErreurInformationsTribu, packet.toByteArray());
    }
}
