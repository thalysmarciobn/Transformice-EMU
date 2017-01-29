package com.transformice.server.helpers;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.server.Server;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Rooms {
    private Server server;
    private Users users;
    private final int[] MapList = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 136, 137, 138, 139, 140, 141, 142, 143, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210};
    private final int[] noShamanMaps = new int[]{7, 8, 14, 22, 23, 28, 29, 54, 55, 57, 58, 59, 60, 61, 70, 77, 78, 87, 88, 92, 122, 123, 124, 125, 126, 1007, 888, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210};
    public ConcurrentHashMap<String, ConcurrentHashMap> channels = new ConcurrentHashMap();

    public Rooms(Server server, Users users) {
        this.server = server;
        this.users = users;
    }

    public void register(String roomName) {
        ConcurrentHashMap info = new ConcurrentHashMap();
        info.put(Identifiers.rooms.players, new ConcurrentHashMap());
        info.put(Identifiers.rooms.mapStatus, 0);
        info.put(Identifiers.rooms.roundTime, 120);
        info.put(Identifiers.rooms.lastCodePartie, 0);
        info.put(Identifiers.rooms.currentMap, 0);
        info.put(Identifiers.rooms.isCurrentlyPlay, true);
        info.put(Identifiers.rooms.gameStartTime, 0);
        info.put(Identifiers.rooms.gameStartTimeMillis, 0);
        info.put(Identifiers.rooms.Anchors, new String[]{});
        this.channels.put(roomName, info);
    }

    public void mapChange(ConcurrentHashMap room, String roomName) {
        room.replace(Identifiers.rooms.currentMap, this.selectMap());
        room.replace(Identifiers.rooms.lastCodePartie, (Integer) room.get(Identifiers.rooms.lastCodePartie) + 1  % Integer.MAX_VALUE);
        room.replace(Identifiers.rooms.mapStatus, (Integer) room.get(Identifiers.rooms.mapStatus) + 1 % 13);
        room.replace(Identifiers.rooms.isCurrentlyPlay, true);
        room.replace(Identifiers.rooms.gameStartTime, this.server.getTime());
        room.replace(Identifiers.rooms.gameStartTimeMillis, System.currentTimeMillis());

        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.users.startPlay(player, this.channels.get(roomName));
            this.sendAllOld(room, Identifiers.send.old.room.player_respawn, this.users.getPlayerData(player));
        }
    }

    public int selectMap() {
        return this.MapList[ThreadLocalRandom.current().nextInt(this.MapList.length)];
    }

    public void addClient(ConcurrentHashMap player, String roomName) {
        if (!this.channels.contains(roomName)) {
            this.register(roomName);
        }
        ConcurrentHashMap room = this.channels.get(roomName);
        ConcurrentHashMap players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        player.replace(Identifiers.player.roomName, roomName);
        players.put(player.get(Identifiers.player.Username), player);
        this.users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.room.enter_room, new ByteArray().writeBoolean(roomName.startsWith("*") || roomName.startsWith(String.valueOf((char) 3))).writeUTF(roomName).toByteArray());
        player.replace(Identifiers.player.Dead, room.get(Identifiers.rooms.isCurrentlyPlay));
        this.sendAllOthersOld(player, this.channels.get(roomName), Identifiers.send.old.room.player_respawn, this.users.getPlayerData(player));
        this.mapChange(room, roomName);
    }

    public void removeClient(ConcurrentHashMap player, String roomName) {
        ConcurrentHashMap players = (ConcurrentHashMap) this.channels.get(roomName).get(Identifiers.rooms.players);
        if (players.contains(player.get(Identifiers.player.Username))) {
            player.remove(player.get(Identifiers.player.Username));
        }
    }

    public void sendAllOthersOld(ConcurrentHashMap sender, ConcurrentHashMap room, int[] identifiers, Object... packet) {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            if (!player.equals(sender)) {
                this.server.users.sendOldPacket((Channel) player.get(Identifiers.player.Channel), identifiers, packet);
            }
        }
    }

    public void sendAllOthers(ConcurrentHashMap sender, ConcurrentHashMap room, int[] identifiers, byte[] packet) {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            if (!player.equals(sender)) {
                this.server.users.sendPacket((Channel) player.get(Identifiers.player.Channel), identifiers, packet);
            }
        }
    }

    public void sendAllOld(ConcurrentHashMap room, int[] identifiers, Object... packet) {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.server.users.sendOldPacket((Channel) player.get(Identifiers.player.Channel), identifiers, packet);
        }
    }

    public void sendAll(ConcurrentHashMap room, int[] identifiers, byte... packet) {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.server.users.sendPacket((Channel) player.get(Identifiers.player.Channel), identifiers, packet);
        }
    }

    public int getPlayerCount(ConcurrentHashMap room) {
        return ((ConcurrentHashMap) room.get(Identifiers.rooms.players)).size();
    }

    public int getRoundTime(ConcurrentHashMap room) {
        return (Integer) room.get(Identifiers.rooms.roundTime);
    }

    public int getSyncCode(ConcurrentHashMap room) {
        return 1;
    }

    public Object[] getPlayerList(ConcurrentHashMap room, int playerCount) {
        List<String> result = new ArrayList(playerCount);
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            result.add(StringUtils.join(new Object[]{player.get(Identifiers.player.Username), player.get(Identifiers.player.Code), 1, (Boolean) player.get(Identifiers.player.Dead) ? 1 : 0, player.get(Identifiers.player.Score), (Boolean) player.get(Identifiers.player.hasCheese) ? 1 : 0, "0,1", 0, player.get(Identifiers.player.Look), 0, player.get(Identifiers.player.Color), player.get(Identifiers.player.ShamanColor), 0},"#"));
        }
        return result.toArray();
    }
}
