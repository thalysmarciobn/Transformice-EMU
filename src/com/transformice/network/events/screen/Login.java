package com.transformice.network.events.screen;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.config.Config;
import com.transformice.server.users.Users;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
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
        Channel channel = (Channel) player.get(Identifiers.player.Channel);
        boolean canLogin = true;
        for (int key : Config.loginKeys) {
            authKey ^= key;
        }
        if (!playerName.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$") || playerName.length() > 25 || (playerName.length() >= 1 && playerName.substring(1).contains("+"))) {
            channel.close();
        } else if (authKey == resultKey && player.get(Identifiers.player.Username).toString().isEmpty()) {
            playerName = playerName.equals("") ? "Souris" : playerName;
            if (users.players.containsKey(playerName)) {
                users.sendPacket(channel, Identifiers.send.screen.login_result, 1);
            } else {
                if (!password.equals("")) {
                    users.server.database.jdbc.beginTransaction();
                    try {
                        QueryResult result = users.server.database.jdbc.query("select * from Users where Username = ? and Password = ?", playerName, password);
                        if (result.next()) {
                            player.put(Identifiers.player.Guest, false);
                            player.put(Identifiers.player.playerID, result.getInt("id"));
                            player.replace(Identifiers.player.Username, result.getString("Username"));
                            player.put(Identifiers.player.PrivilegeLevel, result.getInt("Privilege"));
                            player.put(Identifiers.player.Look, result.getString("Look"));
                            player.put(Identifiers.player.shamanType, result.getInt("ShamanType"));
                            player.put(Identifiers.player.Exp, result.getInt("Experience"));
                            player.put(Identifiers.player.Badge, result.getInt("Badge"));
                            player.put(Identifiers.player.Color, result.getString("Color"));
                            player.put(Identifiers.player.ShamanColor, result.getString("ShamanColor"));
                            player.put(Identifiers.player.FirstCount, result.getString("FirstCount"));
                            player.put(Identifiers.player.CheeseCount, result.getString("CheeseCount"));
                            player.put(Identifiers.player.ShamanSaves, result.getString("ShamanSaves"));
                            player.put(Identifiers.player.HardModeSaves, result.getString("HardModeSaves"));
                            player.put(Identifiers.player.DivineModeSaves, result.getString("DivineModeSaves"));
                            player.put(Identifiers.player.BootcampCount, result.getString("BootcampCount"));
                        } else {
                            users.sendPacket(channel, Identifiers.send.screen.login_result, 2);
                            canLogin = false;
                        }
                        result.close();
                    } catch (JdbcException error) {
                        users.sendPacket(channel, Identifiers.send.screen.login_result, 6);
                        if (users.server.database.jdbc.isInTransaction()) {
                            users.server.database.jdbc.rollbackTransaction();
                        }
                        canLogin = false;
                    } finally {
                        if (users.server.database.jdbc.isInTransaction()) {
                            users.server.database.jdbc.commitTransaction();
                        }
                    }
                } else {
                    player.put(Identifiers.player.playerID, 0);
                    player.replace(Identifiers.player.Username, playerName);
                    player.put(Identifiers.player.PrivilegeLevel, 0);
                    player.put(Identifiers.player.Guest, true);
                    player.put(Identifiers.player.Look, "1;0,0,0,0,0,0,0,0,0");
                    player.put(Identifiers.player.shamanType, 0);
                    player.put(Identifiers.player.Exp, 0);
                    player.put(Identifiers.player.Badge, 0);
                    player.put(Identifiers.player.Color, "dfd8ce");
                    player.put(Identifiers.player.ShamanColor, "95d9d6");
                }
                if (canLogin) {
                    player.put(Identifiers.player.Code, users.lastPlayerCode++);
                    player.put(Identifiers.player.Dead, true);
                    player.put(Identifiers.player.Score, 0);
                    player.put(Identifiers.player.hasCheese, false);
                    player.put(Identifiers.player.isShaman, false);
                    player.put(Identifiers.player.isReceivedDummy, false);
                    player.put(Identifiers.player.isMovingRight, false);
                    player.put(Identifiers.player.isMovingLeft, false);
                    player.put(Identifiers.player.isAfk, true);
                    player.put(Identifiers.player.isSync, false);
                    player.put(Identifiers.player.roomName, "");
                    users.skills.sendShamanSkills(channel, false);
                    users.skills.sendExp(channel, 1, 0, 2);
                    if ((Boolean) player.get(Identifiers.player.Guest)) {
                        users.sendPacket(channel, Identifiers.send.player.login_souris, new ByteArray().writeByte(1).writeByte(10).toByteArray());
                        users.sendPacket(channel, Identifiers.send.player.login_souris, new ByteArray().writeByte(2).writeByte(5).toByteArray());
                        users.sendPacket(channel, Identifiers.send.player.login_souris, new ByteArray().writeByte(3).writeByte(15).toByteArray());
                        users.sendPacket(channel, Identifiers.send.player.login_souris, new ByteArray().writeByte(4).writeByte(200).toByteArray());
                    }
                    users.sendPacket(channel, Identifiers.send.player.player_identification, new ByteArray().writeInt((Integer) player.get(Identifiers.player.playerID)).writeUTF((String) player.get(Identifiers.player.Username)).writeInt(600000).writeByte((Integer) player.get(Identifiers.player.langueByte)).writeInt((Integer) player.get(Identifiers.player.Code)).writeByte((Integer) player.get(Identifiers.player.PrivilegeLevel)).writeByte(0).writeBoolean(false).toByteArray());
                    users.sendShamanItems(channel, player);
                    users.sendPacket(channel, Identifiers.send.player.time_stamp, new ByteArray().writeInt(users.server.getTime()).toByteArray());
                    users.sendPacket(channel, Identifiers.send.player.email_confirmed, 1);
                    users.server.tribulle.sendPlayerInfo(player);
                    users.server.tribulle.sendFriendList(player, null);
                    users.server.tribulle.sendIgnoredsList(player);
                    users.server.tribulle.sendTribe(player, false);
                    users.sendInventoryConsumables(channel, player);
                    users.enterRoom(player, player.get(Identifiers.player.Langue) + "-thalys");

                    users.server.users.players.put(playerName, player);
                }
            }
        }
    }
}
