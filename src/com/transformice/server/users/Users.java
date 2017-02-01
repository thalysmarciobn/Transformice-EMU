package com.transformice.server.users;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.Server;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;


public class Users {

    public ConcurrentHashMap players = new ConcurrentHashMap();

    public int lastPlayerCode = 1;

    public Server server;
    public Commands commands;
    public Skills skills;
    public PacketManage packetManage;

    public Users(Server server) {
        this.server = server;
    }

    public void parsePackets(ConcurrentHashMap player, byte[] tokens, ByteArray packet, int packetID) {
        player.replace(Identifiers.player.lastPacketID, ((Integer) player.get(Identifiers.player.lastPacketID) + 1) % 100);
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
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) data[i];
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
        roomName = roomName.replace("<", "&lt;");
        this.sendRoomGameMode(channel, gameMode, serverGame);
        this.sendEnterRoom(channel, roomName);
        this.sendPacket(channel, new int[]{29, 1}, "");
        player.replace(Identifiers.player.roomName, roomName);
        this.server.rooms.addClient(player, roomName);
        ConcurrentHashMap rooms = (ConcurrentHashMap) this.server.rooms.channels.get(roomName);
        this.sendOldPacket(channel, Identifiers.send.old.room.anchors, (Object[]) rooms.get(Identifiers.rooms.Anchors));
    }

    public void sendRoomGameMode(Channel channel, int gameMode, int serverGame) {
        this.sendPacket(channel, Identifiers.send.room.room_game_mode, serverGame);
        this.sendPacket(channel, Identifiers.send.room.room_type, gameMode);
    }

    public void sendEnterRoom(Channel channel, String roomName) {
        this.sendPacket(channel, Identifiers.send.room.enter_room, new ByteArray().writeBoolean(roomName.startsWith("*") || roomName.startsWith(String.valueOf((char) 3))).writeUTF(roomName).toByteArray());
    }

    public void resetPlayer(ConcurrentHashMap player) {
        player.replace(Identifiers.player.Dead, false);
        player.replace(Identifiers.player.hasCheese, false);
        player.replace(Identifiers.player.isAfk, true);
        player.replace(Identifiers.player.isReceivedDummy, false);
        player.replace(Identifiers.player.Score, player.get(Identifiers.player.Score));
    }

    public void startPlayer(ConcurrentHashMap player, ConcurrentHashMap room) {
        Channel channel = (Channel) player.get(Identifiers.player.Channel);
        this.sendPacket(channel, Identifiers.send.room.new_map, new ByteArray().writeInt((Integer) room.get(Identifiers.rooms.currentMap)).writeShort(this.server.rooms.getPlayerCount(room)).writeByte((Integer) room.get(Identifiers.rooms.lastCodePartie)).writeUTF("").writeUTF("").writeByte(0).writeByte(0).toByteArray());
        int shamanCode, shamanCode2 = 0;
        if (ArrayUtils.contains(this.server.rooms.doubleShamanMaps, (Integer) room.get(Identifiers.rooms.currentMap)) && this.server.rooms.getPlayerCount(room) >= 2) {
            int[] shamans = this.server.rooms.getDoubleShamanCode(room);
            shamanCode = shamans[0];
            shamanCode2 = shamans[1];
        } else {
            shamanCode = this.server.rooms.getShamanCode(room);
        }
        if ((Integer) player.get(Identifiers.player.Code) == shamanCode || (Integer) player.get(Identifiers.player.Code) == shamanCode2) {
            player.replace(Identifiers.player.isShaman, true);
        } else {
            player.replace(Identifiers.player.isShaman, false);
        }
        this.sendPlayerList(channel, room);
            this.sendPacket(channel, Identifiers.send.room.shaman_info, new ByteArray().writeInt((Integer) room.get(Identifiers.rooms.currentShamanCode)).writeInt((Integer) room.get(Identifiers.rooms.currentShamanCode2)).writeByte((Integer) room.get(Identifiers.rooms.currentShamanType)).writeByte((Integer) room.get(Identifiers.rooms.currentShamanType2)).writeShort((Integer) room.get(Identifiers.rooms.currentShamanLevel)).writeShort((Integer) room.get(Identifiers.rooms.currentShamanLevel2)).writeShort((Integer) room.get(Identifiers.rooms.currentShamanBadge)).writeShort((Integer) room.get(Identifiers.rooms.currentShamanBadge2)).toByteArray());
        int sync = this.server.rooms.getSyncCode(room);
        this.sendSync(channel, sync);
        if ((Integer) player.get(Identifiers.player.Code) == sync) {
            player.replace(Identifiers.player.isSync, true);
        } else {
            player.replace(Identifiers.player.isSync, false);
        }
        this.sendPacket(channel, Identifiers.send.room.round_time, new ByteArray().writeShort(this.server.rooms.getRoundTime(room) + ((Integer) room.get(Identifiers.rooms.gameStartTime) - this.server.getTime())).toByteArray());
        if ((Boolean) room.get(Identifiers.rooms.isCurrentlyPlay)) {
            this.sendMapAccess(channel, 0);
        } else {
            this.sendMapAccess(channel, 1);
        }
    }

    public void sendPlayerDisconnect(ConcurrentHashMap room, int playerCode) {
        this.server.rooms.sendAllOld(room, Identifiers.send.old.room.player_disconnect, playerCode);
    }

    public void sendSync(Channel channel, int sync) {
        this.sendOldPacket(channel, Identifiers.send.old.room.sync, new Object[]{sync});
    }

    public void sendMapAccess(Channel channel, int value) {
        this.sendPacket(channel, Identifiers.send.room.map_start_timer, value);
    }

    public void sendPlayerList(Channel channel, ConcurrentHashMap room) {
        this.sendOldPacket(channel, Identifiers.send.old.room.player_list, this.server.rooms.getPlayerList(room, this.server.rooms.getPlayerCount(room)));
    }

    public String getPlayerData(ConcurrentHashMap player) {
        return StringUtils.join(new Object[]{player.get(Identifiers.player.Username), player.get(Identifiers.player.Code), 1, (Boolean) player.get(Identifiers.player.Dead) ? 1 : 0, player.get(Identifiers.player.Score), (Boolean) player.get(Identifiers.player.hasCheese) ? 1 : 0, "0,1", 0, player.get(Identifiers.player.Look), 0, player.get(Identifiers.player.Color), player.get(Identifiers.player.ShamanColor), 0},"#");
    }

    public void sendShamanItems(Channel channel, ConcurrentHashMap player) {
        ByteArray packet = new ByteArray();
        packet.writeShort(0); // size
        this.sendPacket(channel, Identifiers.send.player.shaman_items, packet.toByteArray());
    }

    public void sendInventoryConsumables(Channel channel, ConcurrentHashMap player) {
        ByteArray packet = new ByteArray();
        packet.writeShort(0); // size
        this.sendPacket(channel, Identifiers.send.player.inventory, packet.toByteArray());
    }

    public void sendMessage(Channel channel, String message) {
        this.sendMessage(channel, message, false);
    }

    private void sendMessage(Channel channel, String message, boolean toTab) {
        this.sendPacket(channel, Identifiers.send.player.recv_message, new ByteArray().writeBoolean(toTab).writeUTF(message).writeByte(0).writeUTF("").toByteArray());
    }

    public void sendProfile(Channel channel, String playerName) {
        ConcurrentHashMap player = (ConcurrentHashMap) this.players.get(playerName);
        if (player != null && !(Boolean) player.get(Identifiers.player.Guest)) {

        }
    }

}
