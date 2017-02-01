package com.transformice.server;

import com.transformice.network.Bootstrap;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.cache.ManageCache;
import com.transformice.server.config.Config;
import com.transformice.server.database.Database;
import com.transformice.server.helpers.*;
import com.transformice.server.rooms.Rooms;
import com.transformice.server.tribulle.Tribulle;
import com.transformice.server.users.Commands;
import com.transformice.server.users.Skills;
import com.transformice.server.users.Users;
import jdbchelper.QueryResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Server {
    public List<Channel> channels = new ArrayList();
    public List<Integer> ports = new ArrayList();

    public Long startServer;

    public Langues langues;
    public Database database;
    public Rooms rooms;
    public Users users;
    public Tribulle tribulle;
    public Network network;
    public ManageCache cache;

    private ScheduledExecutorService tasks = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public void start() {
        this.cache = new ManageCache();
        this.println("Connecting to database...","info");
        this.database = new Database();
        if (this.database.connect()) {
            this.scheduleTask(()-> this.database.freeIdleConnections(), 0, 30L, TimeUnit.SECONDS, true);
            this.startServer = System.nanoTime();
            this.println("Strating server...", "info");
            this.langues = new Langues();
            this.users = new Users(this);
            this.users.packetManage = new PacketManage(this.users);
            this.users.commands = new Commands(this.users, this.rooms);
            this.users.skills = new Skills(this.users, this.rooms);
            this.network = new Network();
            this.tribulle = new Tribulle(this, this.users);
            this.rooms = new Rooms(this, this.users);
            this.println("Server loaded in: " + ((System.nanoTime() - this.startServer) / 1000000) + "ms", "info");
            for (int port : new int[]{57}) {
                this.channels.add(new Bootstrap(this).boot().bind(new InetSocketAddress(port)));
                this.ports.add(port);
            }
            this.println("Server online on ports: " + this.ports.toString(), "info");
            this.scheduleTask(()-> this.database.freeIdleConnections(), 30L, TimeUnit.SECONDS, true);
        }
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

    public void println(String message, String type) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [" + type + "] " + message);
    }

    public int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public int getShamanLevelByExperience(int experience) {
        return (experience / Config.Shaman.expBase) + 1;
    }

    public int getNextExperienceByShamanLevel(int level) {
        return level * Config.Shaman.expBase;
    }

    public String getRandom(int size) {
        return RandomStringUtils.random(size, "ABCDE");
    }

    public String getRandomChars(int size) {
        return RandomStringUtils.random(size, "ABCDEF123456789");
    }

    public boolean checkExistingUser(String playerName) {
        QueryResult result = this.database.jdbc.query("SELECT id FROM users WHERE Username = ?", playerName);
        if (result.next()) {
            return true;
        }
        return false;
    }
}
