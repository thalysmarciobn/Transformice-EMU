package com.transformice.server;

import com.transformice.network.Bootstrap;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.helpers.Rooms;
import com.transformice.server.helpers.Sessions;
import com.transformice.server.helpers.Tribulle;
import com.transformice.server.helpers.Users;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public List<Channel> channels = new ArrayList();

    public Rooms rooms;
    public Users users;
    public Tribulle tribulle;

    public int[] packetKeys = {55,62,55,25,29,50,5,10,38,32,109,100,105,71,71,104,99,108,76,74};
    public int[] loginKeys = {-2147483648,-2147483648,256,16777216,13326141,256,16777216,10915256};

    public void start() {
        this.users = new Users(this);
        this.users.sessions = new Sessions();
        this.users.packetManage = new PacketManage();
        this.tribulle = new Tribulle(this, this.users);
        this.rooms = new Rooms(this, this.users);
        for (int port : new int[] {57}) {
            this.channels.add(new Bootstrap(this).boot().bind(new InetSocketAddress(port)));
        }
    }

    public void addClientToRoom(ConcurrentHashMap player, String roomName) {
        roomName = roomName.replace("<", "&lt;");
        if (!this.rooms.channels.contains(roomName)) {
            this.rooms.register(roomName);
        }
        this.rooms.addClient(player, roomName);
    }

    public int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
