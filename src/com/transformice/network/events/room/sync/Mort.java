package com.transformice.network.events.room.sync;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.rooms.threads.MapChange;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@PacketEvent(C = Identifiers.recv._4.C, CC = Identifiers.recv._4.mort)
public class Mort implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        int codePartie = packet.readInt();
        ConcurrentHashMap room = (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName));
        if (codePartie == (Integer) room.get(Identifiers.rooms.lastCodePartie)) {
            player.replace(Identifiers.player.Dead, true);
            player.replace(Identifiers.player.Score, (Integer) player.get(Identifiers.player.Score) + 1);
            player.replace(Identifiers.player.hasCheese, false);
            users.server.rooms.sendAllOld(room, Identifiers.send.room.player_died, player.get(Identifiers.player.Code), 0, player.get(Identifiers.player.Score));
            room.replace(Identifiers.rooms.deathCount, (Integer) room.get(Identifiers.rooms.deathCount) + 1);
            if ((Integer) room.get(Identifiers.rooms.deathCount) >= (Integer) room.get(Identifiers.rooms.playerCount)) {
                ScheduledFuture<?> task = (ScheduledFuture<?>) room.get(Identifiers.rooms.mapChange);
                task.cancel(true);
                room.replace(Identifiers.rooms.mapChange, users.server.rooms.scheduleTask(new MapChange(users.server.rooms, room), 0L, (Integer) room.get(Identifiers.rooms.roundTime), TimeUnit.SECONDS, true));
            }
        }
    }
}
