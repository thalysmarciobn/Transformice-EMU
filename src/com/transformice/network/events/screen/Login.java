package com.transformice.network.events.screen;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.config.Config;
import com.transformice.server.users.Users;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._26.C, CC = Identifiers.recv._26.Login)
public class Login implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        packet = users.packetManage.decrypt(packetID, packet, Config.packetKeys);
        String playerName = users.parsePlayerName(packet.readUTF());
        String password = packet.readUTF();
        player.put(Identifiers.player.Link, packet.readUTF());
        String startRoom = packet.readUTF();
        int resultKey = packet.readInt();
        int authKey = (Integer) player.get(Identifiers.player.AuthKey);
        for (int key : Config.loginKeys) {
            authKey ^= key;
        }
        if (!playerName.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$") || playerName.length() > 25 || (playerName.length() >= 1 && playerName.substring(1).contains("+"))) {
            ((Channel) player.get(Identifiers.player.Channel)).close();
        } else if (authKey == resultKey && player.get(Identifiers.player.Username).toString().isEmpty()) {
            playerName = playerName.equals("") ? "Souris" : playerName;
            System.out.println(authKey + " : " + resultKey);
            if (password.equals("")) {
                player.put(Identifiers.player.Guest, true);
            } else {
                player.put(Identifiers.player.Guest, false);
            }
            if (users.players.contains(playerName)) {
            } else {
                player.put(Identifiers.player.playerID, 0);
                player.replace(Identifiers.player.Username, playerName);
                player.put(Identifiers.player.Code, users.lastPlayerCode++);
                player.put(Identifiers.player.PrivilegeLevel, 0);
                player.put(Identifiers.player.Dead, false);
                player.put(Identifiers.player.Score, 0);
                player.put(Identifiers.player.hasCheese, false);
                player.put(Identifiers.player.isReceivedDummy, false);
                player.put(Identifiers.player.isMovingRight, false);
                player.put(Identifiers.player.isMovingLeft, false);
                player.put(Identifiers.player.isAfk, false);
                player.put(Identifiers.player.isSync, false);
                player.put(Identifiers.player.Look, "1;0,0,0,0,0,0,0,0,0");
                player.put(Identifiers.player.Color, "78583a");
                player.put(Identifiers.player.ShamanColor, "95d9d6");
                player.put(Identifiers.player.roomName, "");
                users.skills.sendShamanSkills((Channel) player.get(Identifiers.player.Channel), false);
                users.skills.sendExp((Channel) player.get(Identifiers.player.Channel), 1, 0, 2);
                if ((Boolean) player.get(Identifiers.player.Guest)) {
                    users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.login_souris, new ByteArray().writeByte(1).writeByte(10).toByteArray());
                    users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.login_souris, new ByteArray().writeByte(2).writeByte(5).toByteArray());
                    users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.login_souris, new ByteArray().writeByte(3).writeByte(15).toByteArray());
                    users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.login_souris, new ByteArray().writeByte(4).writeByte(200).toByteArray());
                }
                users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.player_identification, new ByteArray().writeInt((Integer) player.get(Identifiers.player.playerID)).writeUTF((String) player.get(Identifiers.player.Username)).writeInt(600000).writeByte((Integer) player.get(Identifiers.player.langueByte)).writeInt((Integer) player.get(Identifiers.player.Code)).writeByte((Integer) player.get(Identifiers.player.PrivilegeLevel)).writeByte(0).writeBoolean(false).toByteArray());
                users.sendShamanItems((Channel) player.get(Identifiers.player.Channel), player);
                users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.time_stamp, new ByteArray().writeInt(users.server.getTime()).toByteArray());
                users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.player.email_confirmed, 1);
                users.server.tribulle.sendPlayerInfo(player);
                users.server.tribulle.sendFriendList(player, null);
                users.server.tribulle.sendIgnoredsList(player);
                users.server.tribulle.sendTribe(player, false);
                users.sendInventoryConsumables((Channel) player.get(Identifiers.player.Channel), player);
                users.enterRoom(player, player.get(Identifiers.player.Langue) + "-thalys");

                users.server.users.players.add(playerName);
            }
        }
    }
}
