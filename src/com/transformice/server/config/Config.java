package com.transformice.server.config;

public class Config {

    public static boolean debug = true;

    public static class Shaman {
        public static int expBase = 24;
        public static int maxLevel = 100;
    }

    public static class MySQL {
        public static String Host = "127.0.0.1";
        public static String User = "thalys";
        public static String Password = "123";
        public static String DatabaseName = "thalys_transformice";
        public static int Port = 3306;
        public static int MaxConnections = 2;
    }

    public static int[] packetKeys = {55,62,55,25,29,50,5,10,38,32,109,100,105,71,71,104,99,108,76,74};
    public static int[] loginKeys = {-2147483648,-2147483648,256,16777216,13326141,256,16777216,10915256};
}
