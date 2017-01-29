package com.transformice.network.packet;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketEvent {
    int C();

    int CC();
}
