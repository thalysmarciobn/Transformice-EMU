package com.transformice.network.packet;

import com.transformice.network.events.screen.Login;

import java.util.HashMap;
import java.util.Map;

public class PacketManage {
    public Map<Integer, Packet> packets = new HashMap<>();

    public PacketManage() {
        this.registry(new Login());
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
