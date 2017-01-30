package com.transformice.server.rooms.threads;

import com.transformice.network.packet.Identifiers;
import com.transformice.server.rooms.Rooms;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class MapChange implements Runnable {

    private Rooms rooms;
    private ConcurrentHashMap room;
    private String roomName;

    public MapChange(Rooms rooms, ConcurrentHashMap room, String roomName) {
        this.rooms = rooms;
        this.room = room;
        this.roomName = roomName;
    }

    @Override
    public void run() {
        room.replace(Identifiers.rooms.currentMap, this.rooms.selectMap());
        room.replace(Identifiers.rooms.lastCodePartie, (Integer) room.get(Identifiers.rooms.lastCodePartie) + 1 % Integer.MAX_VALUE);
        room.replace(Identifiers.rooms.mapStatus, (Integer) room.get(Identifiers.rooms.mapStatus) + 1 % 13);
        room.replace(Identifiers.rooms.isCurrentlyPlay, false);
        room.replace(Identifiers.rooms.gameStartTime, this.rooms.server.getTime());
        room.replace(Identifiers.rooms.gameStartTimeMillis, System.currentTimeMillis());

        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        room.replace(Identifiers.rooms.isCurrentlyPlay, false);
        for (ConcurrentHashMap player : players.values()) {
            this.rooms.users.resetPlayer(player, (ConcurrentHashMap) this.rooms.channels.get(roomName));
            this.rooms.users.startPlayer(player, (ConcurrentHashMap) this.rooms.channels.get(roomName));
        }
    }
}
