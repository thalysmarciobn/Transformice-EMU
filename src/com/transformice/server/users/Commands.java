package com.transformice.server.users;

import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.PacketException;
import com.transformice.server.rooms.Rooms;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Arrays;

public class Commands {
    private Users users;
    private Rooms rooms;

    public Commands(Users users, Rooms rooms) {
        this.users = users;
        this.rooms = rooms;
    }

    public void parse(ConcurrentHashMap player, String command) throws PacketException {
        String[] values = StringUtils.split(command, " ");
        command = values[0].toLowerCase();
        String[] arguments = Arrays.copyOfRange(values, 1, values.length);
        String argument = StringUtils.join(arguments, " ");
        Channel channel = (Channel) player.get(Identifiers.player.Channel);
        if (arguments.length == 0) {
            if (command.equals("about")) {
                StringBuilder sb = new StringBuilder();
                sb.append("Transformice Emulator\n");
                sb.append("Players: ").append(users.players.size()).append("\n");
                sb.append("Rooms: ").append(users.server.rooms.channels.size()).append("\n");
                users.sendMessage(channel, sb.toString());
            } else if (command.equals("profil") || command.equals("perfil") || command.equals("profile")) {
                users.sendProfile(channel, (String) player.get(Identifiers.player.Username));
            }
        }
        //throw new PacketException("<R>test</R>");
    }
}
