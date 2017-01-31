package com.transformice.network.events.room.sync;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._4.C, CC = Identifiers.recv._4.mouse_movement)
public class MouseMovement implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        int codePartie = packet.readInt();
        boolean droiteEnCours = packet.readBoolean();
        boolean gaucheEnCours = packet.readBoolean();
        int px = packet.readUnsignedShort();
        int py = packet.readUnsignedShort();
        int vx = packet.readUnsignedShort();
        int vy = packet.readUnsignedShort();
        boolean jump = packet.readBoolean();
        byte jump_img = packet.readByte();
        byte portal = packet.readByte();
        boolean isAngle = packet.bytesAvailable();
        int angle = isAngle ? packet.readUnsignedShort() : -1;
        int vel_angle = isAngle ? packet.readUnsignedShort() : -1;
        boolean loc1 = isAngle ? packet.readBoolean() : false;
        ConcurrentHashMap room = (ConcurrentHashMap) users.server.rooms.channels.get(player.get(Identifiers.player.roomName));
        if (codePartie == (Integer) room.get(Identifiers.rooms.lastCodePartie)) {
            if (droiteEnCours || gaucheEnCours) {
                player.replace(Identifiers.player.isMovingRight, droiteEnCours);
                player.replace(Identifiers.player.isMovingLeft, gaucheEnCours);
                if ((Boolean) player.get(Identifiers.player.isAfk)) {
                    player.replace(Identifiers.player.isAfk, false);
                }
                player.replace(Identifiers.player.posX, px * 800 / 2700);
                player.replace(Identifiers.player.posY, py * 800 / 2700);
                player.replace(Identifiers.player.velX, vx);
                player.replace(Identifiers.player.velY, vy);
                player.replace(Identifiers.player.isJumping, jump);
            }
            ByteArray packet2 = new ByteArray().writeInt((Integer) player.get(Identifiers.player.Code)).writeInt(codePartie).writeBoolean(droiteEnCours).writeBoolean(gaucheEnCours).writeShort(px).writeShort(py).writeShort(vx).writeShort(vy).writeBoolean(jump).writeByte(jump_img).writeByte(portal);
            if (isAngle) {
                packet2.writeShort(angle).writeShort(vel_angle).writeBoolean(loc1);
            }
            users.server.rooms.sendAllOthers(player, room, Identifiers.send.room.player_movement, packet2.toByteArray());
        }
    }
}
