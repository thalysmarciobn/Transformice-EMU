package com.transformice.server.rooms.threads;

import com.transformice.network.packet.Identifiers;
import com.transformice.server.rooms.Rooms;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.concurrent.TimeUnit;

public class MapChange implements Runnable {

    private Rooms rooms;
    private ConcurrentHashMap room;

    public MapChange(Rooms rooms, ConcurrentHashMap room) {
        this.rooms = rooms;
        this.room = room;
    }

    @Override
    public void run() {
        this.rooms.getSyncCode(this.room);
        this.room.replace(Identifiers.rooms.Anchors, new String[]{});
        this.room.replace(Identifiers.rooms.currentMap, this.rooms.selectMap());
        this.room.replace(Identifiers.rooms.lastCodePartie, (Integer) this.room.get(Identifiers.rooms.lastCodePartie) + 1 % Integer.MAX_VALUE);
        this.room.replace(Identifiers.rooms.mapStatus, (Integer) this.room.get(Identifiers.rooms.mapStatus) + 1 % 13);
        this.room.replace(Identifiers.rooms.isCurrentlyPlay, false);
        this.room.replace(Identifiers.rooms.gameStartTime, this.rooms.server.getTime());
        this.room.replace(Identifiers.rooms.gameStartTimeMillis, System.currentTimeMillis());
        this.room.replace(Identifiers.rooms.isCurrentlyPlay, false);
        this.room.replace(Identifiers.rooms.currentShamanCode, -1);
        this.room.replace(Identifiers.rooms.currentShamanType, 0);
        this.room.replace(Identifiers.rooms.currentShamanLevel, 0);
        this.room.replace(Identifiers.rooms.currentShamanBadge, 0);
        this.room.replace(Identifiers.rooms.currentShamanName, "");
        this.room.replace(Identifiers.rooms.currentShamanCode2, -1);
        this.room.replace(Identifiers.rooms.currentShamanType2, 0);
        this.room.replace(Identifiers.rooms.currentShamanLevel2, 0);
        this.room.replace(Identifiers.rooms.currentShamanBadge2, 0);
        this.room.replace(Identifiers.rooms.currentShamanName2, "");
        this.room.replace(Identifiers.rooms.deathCount, 0);
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.rooms.users.resetPlayer(player);
        }
        for (ConcurrentHashMap player : players.values()) {
            this.rooms.users.startPlayer(player, this.room);
        }
        this.room.replace(Identifiers.rooms.playerCount, players.size());
        this.rooms.scheduleTask(()-> this.closeRoom(), 3L, TimeUnit.SECONDS);
    }

    public void closeRoom() {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.rooms.users.sendMapAccess((Channel) player.get(Identifiers.player.Channel), 0);
        }
        this.room.replace(Identifiers.rooms.isCurrentlyPlay, true);
    }
}
