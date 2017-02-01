package com.transformice.network.packet;

import com.transformice.network.events.chat.Commands;
import com.transformice.network.events.old.OldProtocol;
import com.transformice.network.events.room.sync.*;
import com.transformice.network.events.screen.Captcha;
import com.transformice.network.events.screen.CreateAccount;
import com.transformice.network.events.screen.Langue;
import com.transformice.network.events.screen.Login;
import com.transformice.server.config.Config;
import com.transformice.server.users.Users;

import java.util.HashMap;
import java.util.Map;

public class PacketManage {
    public Map<Integer, Packet> packets = new HashMap<>();

    private Users users;

    public PacketManage(Users users) {
        this.users = users;
        this.registry(new Login());
        this.registry(new CreateAccount());
        this.registry(new Langue());
        this.registry(new Captcha());
        this.registry(new Mort());
        this.registry(new MouseMovement());
        this.registry(new ShamanPosition());
        this.registry(new PlayerPosition());
        this.registry(new Crouch());
        this.registry(new ObjectSync());
        this.registry(new BridgeBreak());
        this.registry(new OldProtocol());
        this.registry(new Commands());
        if (Config.debug) {
            this.users.server.println("Packets loaded: " + this.packets.size(), "debug");
        }
    }

    public ByteArray decrypt(int packetID, ByteArray packet, int[] keys) {
        ByteArray data = new ByteArray();
        while (packet.bytesAvailable()) {
            packetID = ++packetID % keys.length;
            data.writeByte(packet.readByte() ^ keys[packetID]);
        }
        return data;
    }

    private void registry(Packet packet) {
        Class<? extends  Packet> _class = packet.getClass();
        if (_class.getAnnotations().length > 0) {
            PacketEvent event = _class.getAnnotation(PacketEvent.class);
            this.packets.put((event.C() << 8) | event.CC(), packet);
        }
    }
}
