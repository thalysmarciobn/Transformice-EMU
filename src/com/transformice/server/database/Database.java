package com.transformice.server.database;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.transformice.server.config.Config;
import jdbchelper.ConnectionPool;
import jdbchelper.JdbcHelper;
import jdbchelper.PooledDataSource;

import java.sql.SQLException;

public class Database {

    public JdbcHelper jdbc;
    private ConnectionPool pool;
    private MysqlConnectionPoolDataSource source;

    public Database() {
        this.source = new MysqlConnectionPoolDataSource();
    }

    public boolean connect() {
        this.source.setServerName(Config.MySQL.Host);
        this.source.setPort(Config.MySQL.Port);
        this.source.setUser(Config.MySQL.User);
        this.source.setPassword(Config.MySQL.Password);
        this.source.setDatabaseName(Config.MySQL.DatabaseName);
        this.source.setAutoReconnectForConnectionPools(true);
        this.pool = new ConnectionPool(this.source, Config.MySQL.MaxConnections);
        this.jdbc = new JdbcHelper(new PooledDataSource(this.pool));
        return true;
    }

    public void freeIdleConnections() {
        this.pool.freeIdleConnections();
    }

    public int getActiveConnections() {
        return this.pool.getActiveConnections();
    }

    public void shutdown() {
        try {
            this.pool.dispose();
            this.pool = null;
            this.jdbc = null;
        } catch (SQLException error) {

        }
    }
}
