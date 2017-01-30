package com.transformice.server;

import com.transformice.network.Bootstrap;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.config.Config;
import com.transformice.server.database.DatabasePool;
import com.transformice.server.helpers.*;
import com.transformice.server.rooms.Rooms;
import com.transformice.server.tribulle.Tribulle;
import com.transformice.server.users.Skills;
import com.transformice.server.users.Users;
import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    public List<Channel> channels = new ArrayList();
    public List<Integer> ports = new ArrayList();

    public Long startServer;

    public boolean debug = true;

    public int[] packetKeys = {55,62,55,25,29,50,5,10,38,32,109,100,105,71,71,104,99,108,76,74};
    public int[] loginKeys = {-2147483648,-2147483648,256,16777216,13326141,256,16777216,10915256};

    public Langues langues;
    public DatabasePool database;
    public Rooms rooms;
    public Users users;
    public Tribulle tribulle;
    public Network network;

    public void start() {
        this.println("Connecting to database...","info");
        this.database = new DatabasePool();
        this.startServer = System.nanoTime();
        this.println("Strating server...","info");
        this.langues = new Langues();
        this.users = new Users(this);
        this.users.packetManage = new PacketManage(this.users);
        this.users.skills = new Skills(this.users, this.rooms);
        this.network = new Network();
        this.tribulle = new Tribulle(this, this.users);
        this.rooms = new Rooms(this, this.users);
        this.println("Server loaded in: " + ((System.nanoTime() - this.startServer) / 1000000) + "ms", "info");
        for (int port : new int[] {57}) {
            this.channels.add(new Bootstrap(this).boot().bind(new InetSocketAddress(port)));
            this.ports.add(port);
        }
        this.println("Server online on ports: " + this.ports.toString(), "info");
    }

    public void println(String message, String type) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [" + type + "] " + message);
    }

    public int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
