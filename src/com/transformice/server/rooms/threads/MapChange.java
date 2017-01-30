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
        room.replace(Identifiers.rooms.Anchors, new String[]{});
        room.replace(Identifiers.rooms.currentMap, this.rooms.selectMap());
        room.replace(Identifiers.rooms.lastCodePartie, (Integer) this.room.get(Identifiers.rooms.lastCodePartie) + 1 % Integer.MAX_VALUE);
        room.replace(Identifiers.rooms.mapStatus, (Integer) this.room.get(Identifiers.rooms.mapStatus) + 1 % 13);
        room.replace(Identifiers.rooms.isCurrentlyPlay, false);
        room.replace(Identifiers.rooms.gameStartTime, this.rooms.server.getTime());
        room.replace(Identifiers.rooms.gameStartTimeMillis, System.currentTimeMillis());

        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            this.rooms.users.startPlayer(player, this.room);
        }
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
