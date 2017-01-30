package com.transformice.server.database;

import com.transformice.server.Server;
import com.transformice.server.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabasePool {
    private HikariConfig source;

    public HikariDataSource DatabasePool(Server server) {
        this.source = new HikariConfig();
        this.source.setPoolName("transformice");
        this.source.setMaximumPoolSize(20);
        this.source.setInitializationFailFast(true);
        this.source.setJdbcUrl("jdbc:mysql://" + Config.MySQLHost + ":" + Config.MySQLPort + "/" + Config.MySQLData);
        this.source.addDataSourceProperty("serverName", Config.MySQLHost);
        this.source.addDataSourceProperty("port",  Config.MySQLPort);
        this.source.addDataSourceProperty("databaseName", Config.MySQLData);
        this.source.addDataSourceProperty("user", Config.MySQLUser);
        this.source.addDataSourceProperty("password", Config.MySQLPass);
        this.source.addDataSourceProperty("cachePrepStmts", "true");
        this.source.addDataSourceProperty("prepStmtCacheSize", "250");
        this.source.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.source.setAutoCommit(true);
        this.source.setConnectionTimeout(3400L);
        this.source.setValidationTimeout(3399L);
        this.source.setMaxLifetime(30000L);
        this.source.setIdleTimeout(10000L);
        return new HikariDataSource(this.source);
    }
}
