package com.transformice.server.helpers;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.Server;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;


public class Users {

    public List<String> players = new ArrayList();

    public int lastPlayerCode = 1;

    public Server server;
    public Sessions sessions;
    public PacketManage packetManage;

    public Users(Server server) {
        this.server = server;
    }

    public void parsePackets(ConcurrentHashMap player, byte[] tokens, ByteArray packet, int packetID) {
        player.put(Identifiers.player.LastPacket, ((Integer) player.get(Identifiers.player.LastPacket) + 1) % 100);
        int header = (tokens[0] << 8) | tokens[1];
        if (this.packetManage.packets.containsKey(header)) {
            this.packetManage.packets.get(header).parse(this, player, packet, packetID);
        }
    }

    public void sendPacket(Channel channel, int[] identifiers, byte... data) {
        ByteArray packet = new ByteArray();
        int length = data.length + 2;
        if (length <= 0xFF) {
            packet.writeByte(1).writeByte(length);
        } else if (length <= 0xFFFF) {
            packet.writeByte(2).writeShort(length);
        } else if (length <= 0xFFFFFF) {
            packet.writeByte(3).writeByte((length >> 16) & 0xFF).writeByte((length >> 8) & 0xFF).writeByte(length & 0xFF);
        }
        packet.writeByte(identifiers[0]).writeByte(identifiers[1]).writeBytes(data);
        channel.write(ChannelBuffers.wrappedBuffer(packet.toByteArray()));
    }

    public final void sendPacket(Channel channel, int[] identifiers, int... data) {
        byte[] result = new byte[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            result[i] = (byte) identifiers[i];
        }
        this.sendPacket(channel, identifiers, result);
    }

    public void sendPacket(Channel channel, int[] identifiers, String packet) {
        this.sendPacket(channel, identifiers, packet.getBytes());
    }

    public void sendOldPacket(Channel channel, int[] identifiers, Object... values) {
        ByteArray packet = new ByteArray();
        String data = values.length == 0 ? "" : '\u0001' + StringUtils.join(values, '\u0001');
        int length = data.length() + 6;
        if (length <= 0xFF) {
            packet.writeByte(1).writeByte(length);
        } else if (length <= 0xFFFF) {
            packet.writeByte(2).writeShort(length);
        } else if (length <= 0xFFFFFF) {
            packet.writeByte(3).writeByte((length >> 16) & 0xFF).writeByte((length >> 8) & 0xFF).writeByte(length & 0xFF);
        }
        packet.writeByte(1).writeByte(1).writeShort(data.length() + 2).writeByte(identifiers[0]).writeByte(identifiers[1]).writeBytes(data);
        channel.write(ChannelBuffers.wrappedBuffer(packet.toByteArray()));
    }

    public String parsePlayerName(String playerName) {
        return playerName.startsWith("*") ? "*" + StringUtils.capitalize(playerName.substring(1).toLowerCase()) : StringUtils.capitalize(playerName.toLowerCase());
    }

    public void enterRoom(ConcurrentHashMap player, String roomName) {
        Channel channel = (Channel) player.get(Identifiers.player.Channel);

        int gameMode = roomName.startsWith("music") ? 11 : roomName.contains("madchees") ? 1 : 4;
        int serverGame = roomName.contains("madchees") ? 4 : 0;
        this.sendPacket(channel, Identifiers.send.room.room_game_mode, serverGame);
        this.sendPacket(channel, Identifiers.send.room.room_type, gameMode);

        this.sendPacket(channel, new int[]{29, 1}, "");
        this.sendPacket(channel, Identifiers.send.room.enter_room, new ByteArray().writeBoolean(roomName.startsWith("*") || roomName.startsWith(String.valueOf((char) 3))).writeUTF(roomName).toByteArray());

        ConcurrentHashMap room = this.server.rooms.channels.get(roomName);
        this.server.rooms.sendAllOthersOld(player, room, Identifiers.send.old.room.player_respawn, this.getPlayerData(player));
        this.sendOldPacket(channel, Identifiers.send.old.room.anchors, (Object[]) room.get(Identifiers.rooms.Anchors));

        this.sendPacket(channel, Identifiers.send.room.new_map, new ByteArray().writeInt((Integer) room.get(Identifiers.rooms.currentWord)).writeShort(this.server.rooms.getPlayerCount(room)).writeByte((Integer) room.get(Identifiers.rooms.CodePartieEnCours)).writeUTF("").writeUTF("").writeByte(0).writeByte(0).toByteArray());
        this.sendOldPacket(channel, Identifiers.send.old.room.player_list, this.server.rooms.getPlayerList(room, this.server.rooms.getPlayerCount(room)));

        this.sendPacket(channel, Identifiers.send.room.shaman_info, new ByteArray().writeInt(0).writeInt(0).writeByte((Integer) room.get(Identifiers.rooms.currentShamanType)).writeByte((Integer) room.get(Identifiers.rooms.currentSecondShamanType)).writeShort(0).writeShort(0).writeShort(0).writeShort(0).toByteArray());


        this.sendOldPacket(channel, Identifiers.send.old.room.sync, this.server.rooms.getSyncCode(room));
        this.sendPacket(channel, Identifiers.send.room.round_time, new ByteArray().writeShort(this.server.rooms.getRoundTime(room)).toByteArray());
        this.sendPacket(channel, Identifiers.send.room.map_start_timer, 1);
    }

    public String getPlayerData(ConcurrentHashMap player) {
        return StringUtils.join(new Object[]{player.get(Identifiers.player.Username), player.get(Identifiers.player.Code), 1, (Boolean) player.get(Identifiers.player.Dead) ? 1 : 0, player.get(Identifiers.player.Score), (Boolean) player.get(Identifiers.player.hasCheese) ? 1 : 0, "0,1", 0, player.get(Identifiers.player.Look), 0, player.get(Identifiers.player.Color), player.get(Identifiers.player.ShamanColor), 0},"#");
    }
}