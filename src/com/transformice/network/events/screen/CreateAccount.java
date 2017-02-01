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

@PacketEvent(C = Identifiers.recv._26.C, CC = Identifiers.recv._26.Create_Account)
public class CreateAccount implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        packet = users.packetManage.decrypt(packetID, packet, Config.packetKeys);
        String playerName = users.parsePlayerName(packet.readUTF());
        String password = packet.readUTF();
        String captcha = packet.readUTF();
        player.replace(Identifiers.player.Link, packet.readUTF());
        Channel channel = (Channel) player.get(Identifiers.player.Channel);
        // 4 deve começar com letra
        // 5 criar conta recentemente
        // 6 erro intenro
        if (!playerName.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$")) {
            channel.close();
        } else if (!captcha.equals(player.get(Identifiers.player.currentCaptcha))) {
            users.sendPacket(channel, Identifiers.send.screen.login_result, 7);
        } else if (users.server.checkExistingUser(playerName)) {
            users.sendPacket(channel, Identifiers.send.screen.login_result, 3);
        } else {
            System.out.println(playerName);
            users.server.database.jdbc.beginTransaction();
            try {
                users.server.database.jdbc.run("INSERT INTO users (Username, Password, DateCreated, Address) VALUES (?, ?, NOW(), ?)", playerName, password, player.get(Identifiers.player.ipAddress));
                int playerID = Long.valueOf(users.server.database.jdbc.getLastInsertId()).intValue();
                users.server.database.jdbc.run("INSERT INTO users_titles (UserID, TitleID, StarCount) VALUES (?, 0, 1)", playerID);
                users.packetManage.packets.get(Identifiers.recv._26.C << 8).parse(users, player, packet, packetID);
                player.replace(Identifiers.player.isNew, true);
            } catch(JdbcException error){
                users.sendPacket(channel, Identifiers.send.screen.login_result, 6);
                if (users.server.database.jdbc.isInTransaction()) {
                    users.server.database.jdbc.rollbackTransaction();
                }
            } finally{
                if (users.server.database.jdbc.isInTransaction()) {
                    users.server.database.jdbc.commitTransaction();
                }
            }
        }
    }
}
