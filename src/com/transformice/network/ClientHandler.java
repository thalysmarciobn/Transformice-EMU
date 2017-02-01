package com.transformice.network;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.ParsePackets;
import com.transformice.server.Server;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

public class ClientHandler extends SimpleChannelHandler {
    private final Server server;
    private final ParsePackets parse;

    public ClientHandler(Server server) {
        this.server = server;
        this.parse = new ParsePackets(this.server.users);
    }

    @Override
    public void channelOpen(ChannelHandlerContext context, ChannelStateEvent e) {
        ConcurrentHashMap player = new ConcurrentHashMap();
        player.put(Identifiers.player.Channel, context.getChannel());
        player.put(Identifiers.player.isNew, false);
        player.put(Identifiers.player.lastPacketID, 0);
        player.put(Identifiers.player.ipAddress, ((InetSocketAddress) context.getChannel().getRemoteAddress()).getAddress().getHostAddress());
        player.put(Identifiers.player.Username, "");
        player.put(Identifiers.player.LastPacket, ThreadLocalRandom.current().nextInt(0,99));
        player.put(Identifiers.player.AuthKey, ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
        player.put(Identifiers.player.Langue, "BR");
        player.put(Identifiers.player.langueByte, 3);
        player.put(Identifiers.player.currentCaptcha, this.server.getRandom(4));
        context.getChannel().setAttachment(player);
    }

    @Override
    public void channelClosed(ChannelHandlerContext context, ChannelStateEvent e) {
        ConcurrentHashMap player = (ConcurrentHashMap) context.getChannel().getAttachment();
        this.server.rooms.removeClient(player, (String) player.get(Identifiers.player.roomName));
        this.server.users.players.remove(player.get(Identifiers.player.Username));
        player.clear();
    }

    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent e) {
        if ((e.getMessage() instanceof byte[])) {
            byte[] buff = (byte[]) e.getMessage();
            if (buff != null && buff.length > 2) {
                if (this.server.network.checkIncompletePacket(context.getChannel())) {
                    buff = ArrayUtils.addAll(this.server.network.getIncompletePacket(context.getChannel()), buff);
                }
                this.parsePacket(context, new ByteArray(buff), buff);
            }
        }
    }

    public void parsePacket(ChannelHandlerContext context, ByteArray packet, byte[] buff) {
        if (packet.size() > 2) {
            byte sizeBytes = packet.readByte();
            int length = sizeBytes == 1 ? packet.readUnsignedByte() : sizeBytes == 2 ? packet.readUnsignedShort() : sizeBytes == 3 ? ((packet.readUnsignedByte() & 0xFF) << 16) | ((packet.readUnsignedByte() & 0xFF) << 8) | (packet.readUnsignedByte() & 0xFF) : 0;
            if (length != 0) {
                byte packetID = packet.readByte();
                if (packet.size() == length) {
                    if (packet.size() >= 2) {
                        this.parse.parsePacket(context.getChannel(), packet, packetID);
                    }
                } else if (packet.size() < length) {
                    this.server.network.putIncompletePacket(context.getChannel(), buff);
                } else if (packet.size() > length) {
                    byte[] data = packet.read(new byte[length]);
                    if (length >= 2) {
                        this.parse.parsePacket(context.getChannel(), new ByteArray(data), packetID);
                    }
                    if (packet.size() >= 2) {
                        this.parsePacket(context, packet, packet.toByteArray());
                    }
                }
            }
        }
    }
}
