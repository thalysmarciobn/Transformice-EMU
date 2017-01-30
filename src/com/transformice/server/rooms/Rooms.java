package com.transformice.server.rooms;

import com.transformice.network.packet.Identifiers;
import com.transformice.server.Server;
import com.transformice.server.rooms.threads.MapChange;
import com.transformice.server.users.Users;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Rooms {
    public Server server;
    public Users users;
    private final int[] MapList = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 136, 137, 138, 139, 140, 141, 142, 143, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210};
    private final int[] noShamanMaps = new int[]{7, 8, 14, 22, 23, 28, 29, 54, 55, 57, 58, 59, 60, 61, 70, 77, 78, 87, 88, 92, 122, 123, 124, 125, 126, 1007, 888, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210};
    public ConcurrentHashMap channels = new ConcurrentHashMap();
    private ScheduledExecutorService tasks = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public Rooms(Server server, Users users) {
        this.server = server;
        this.users = users;
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu) {
        return this.scheduleTask(task, delay, tu, false);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu, boolean repeat) {
        return repeat ? this.tasks.scheduleAtFixedRate(task, delay, delay, tu) : this.tasks.schedule(task, delay, tu);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long start, long delay, TimeUnit tu, boolean repeat) {
        return repeat ? this.tasks.scheduleAtFixedRate(task, start, delay, tu) : this.tasks.schedule(task, delay, tu);
    }

    public void register(String roomName) {
        ConcurrentHashMap room = new ConcurrentHashMap();
        room.put(Identifiers.rooms.players, new ConcurrentHashMap());
        room.put(Identifiers.rooms.mapStatus, 0);
        room.put(Identifiers.rooms.roundTime, 120);
        room.put(Identifiers.rooms.lastCodePartie, 0);
        room.put(Identifiers.rooms.currentMap, 0);
        room.put(Identifiers.rooms.isCurrentlyPlay, true);
        room.put(Identifiers.rooms.gameStartTime, 0);
        room.put(Identifiers.rooms.gameStartTimeMillis, 0);
        room.put(Identifiers.rooms.Anchors, new String[]{});
        this.channels.put(roomName, room);
        this.scheduleTask(new MapChange(this, room, roomName), 0L, (Integer) room.get(Identifiers.rooms.roundTime), TimeUnit.SECONDS, true);
    }

    public int selectMap() {
        return this.MapList[ThreadLocalRandom.current().nextInt(this.MapList.length)];
    }

    public void addClient(ConcurrentHashMap player, String roomName) {
        if (this.channels.get(roomName) == null) {
            this.register(roomName);
        }
        ConcurrentHashMap room = (ConcurrentHashMap) this.channels.get(roomName);
        ConcurrentHashMap players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        player.replace(Identifiers.player.roomName, roomName);
        players.put(player.get(Identifiers.player.Username), player);
        player.replace(Identifiers.player.Dead, room.get(Identifiers.rooms.isCurrentlyPlay));
    }

    public void removeClient(ConcurrentHashMap player, String roomName) {
        ConcurrentHashMap room = (ConcurrentHashMap) this.channels.get(roomName);
        ConcurrentHashMap players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
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
