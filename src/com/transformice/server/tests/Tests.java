package com.transformice.server.tests;

import com.transformice.server.config.Config;

public class Tests {

    public static void main(String... args) {
        new Tests();
    }

    public Tests() {
        int level = this.getShamanLevelByExperience(48);
        int next = this.getNextExperienceByShamanLevel(level);
        System.out.println(level);
        System.out.println(next);
    }

    private int getShamanLevelByExperience(int experience) {
        int base = Config.Shaman.expBase;
        int level = (experience / base) + 1;
        return level;
    }

    public int getNextExperienceByShamanLevel(int level) {
        int base = Config.Shaman.expBase;
        int next = level * base;
        return next;
    }
}
