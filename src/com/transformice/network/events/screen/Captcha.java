package com.transformice.network.events.screen;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Identifiers;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.users.Users;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;

@PacketEvent(C = Identifiers.recv._26.C, CC = Identifiers.recv._26.Captcha)
public class Captcha implements Packet {

    @Override
    public void parse(Users users, ConcurrentHashMap player, ByteArray packet, int packetID) {
        player.replace(Identifiers.player.currentCaptcha, users.server.getRandom(4));
        BufferedImage image = new BufferedImage(36, 17, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(users.server.cache.font);
        FontMetrics metrics = g2d.getFontMetrics();
        int charsWidth = metrics.charsWidth((Identifiers.player.currentCaptcha).toCharArray(), 0, 4);
        g2d.dispose();
        image.flush();
        image = new BufferedImage(charsWidth + 12, 17, BufferedImage.TYPE_INT_RGB);
        g2d = image.createGraphics();
        g2d.setFont(users.server.cache.font);
        g2d.setBackground(Color.black);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        TextLayout textLayout = new TextLayout((String) player.get(Identifiers.player.currentCaptcha), users.server.cache.font, g2d.getFontRenderContext());
        g2d.setPaint(new Color(205, 205, 205));
        textLayout.draw(g2d, 1, 13);
        textLayout.draw(g2d, 3, 13);
        textLayout.draw(g2d, 2, 14);
        textLayout.draw(g2d, 2, 12);
        textLayout.draw(g2d, 3, 14);
        textLayout.draw(g2d, 1, 12);
        textLayout.draw(g2d, 1, 14);
        textLayout.draw(g2d, 3, 12);
        g2d.setPaint(new Color(0, 0, 0));
        textLayout.draw(g2d, 2, 13);
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelsCount = 0;
        ByteArray pixels = new ByteArray();
        ByteArray p2 = new ByteArray();
        p2.writeShort(width);
        p2.writeShort(height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                pixels.writeInt(new Color(image.getRGB(col, row)).getBlue());
                pixelsCount++;
            }
        }
        g2d.dispose();
        image.flush();
        p2.writeShort(pixelsCount);
        p2.writeByte(0);
        p2.writeBytes(pixels.toByteArray());
        users.sendPacket((Channel) player.get(Identifiers.player.Channel), Identifiers.send.screen.captcha, p2.toByteArray());
    }
}
