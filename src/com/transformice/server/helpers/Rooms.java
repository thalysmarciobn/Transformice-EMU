package com.transformice.server.helpers;

import com.transformice.network.packet.Identifiers;
import com.transformice.server.Server;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;

public class Rooms {
    private Server server;
    private Users users;
    public ConcurrentHashMap<String, ConcurrentHashMap> channels = new ConcurrentHashMap();

    public Rooms(Server server, Users users) {
        this.server = server;
        this.users = users;
    }

    public void register(String roomName) {
        ConcurrentHashMap info = new ConcurrentHashMap();
        info.put(Identifiers.rooms.players, new ConcurrentHashMap());
        info.put(Identifiers.rooms.roundTime, 120);
        info.put(Identifiers.rooms.currentShamanType, -1);
        info.put(Identifiers.rooms.currentSecondShamanType, -1);
        info.put(Identifiers.rooms.currentWord, 1);
        info.put(Identifiers.rooms.CodePartieEnCours, 1);
        info.put(Identifiers.rooms.Anchors, new String[]{});
        info.put(Identifiers.rooms.ISCMdata, new Object[] {0, "Invalid", "<C><P /><Z><S /><D /><O /></Z></C>", 0, 0, 0, 0});
        this.channels.put(roomName, info);
    }

    public void addClient(ConcurrentHashMap player, String roomName) {
        ConcurrentHashMap players = (ConcurrentHashMap) this.channels.get(roomName).get(Identifiers.rooms.players);
        player.replace(Identifiers.player.roomName, roomName);
        players.put(player.get(Identifiers.player.Username), player);
        this.users.enterRoom(player, roomName);
    }

    public void sendAllOthersOld(ConcurrentHashMap sender, ConcurrentHashMap room, int[] identifiers, Object... packet) {
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            if (!player.equals(sender)) {
                this.server.users.sendOldPacket((Channel) player.get(Identifiers.player.Channel), identifiers, packet);
            }
        }
    }

    public int getPlayerCount(ConcurrentHashMap room) {
        return ((ConcurrentHashMap) room.get(Identifiers.rooms.players)).size();
    }

    public int getRoundTime(ConcurrentHashMap room) {
        return (Integer) room.get(Identifiers.rooms.roundTime);
    }

    public int getSyncCode(ConcurrentHashMap room) {
        return 0;
    }

    public Object[] getPlayerList(ConcurrentHashMap room, int playerCount) {
        List<String> result = new ArrayList(playerCount);
        ConcurrentHashMap<String, ConcurrentHashMap> players = (ConcurrentHashMap) room.get(Identifiers.rooms.players);
        for (ConcurrentHashMap player : players.values()) {
            result.add(StringUtils.join(new Object[]{player.get(Identifiers.player.Username), player.get(Identifiers.player.Code), 1, (Boolean) player.get(Identifiers.player.Dead) ? 1 : 0, player.get(Identifiers.player.Score), (Boolean) player.get(Identifiers.player.hasCheese) ? 1 : 0, "0,1", 0, player.get(Identifiers.player.Look), 0, player.get(Identifiers.player.Color), player.get(Identifiers.player.ShamanColor), 0},"#"));
        }
        System.out.println(result.toString());
        return result.toArray();
    }
}
