package de.diddiz.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.hibernate.HikariConfigurationUtil;
import com.zaxxer.hikari.pool.HikariPool;
import de.diddiz.LogBlock.config.Config;
import de.mint.asyncmysqlpoolhandler.configservice.ConfigBuilder;
import de.mint.asyncmysqlpoolhandler.configservice.ConfigPoolFramework;
import de.mint.asyncmysqlpoolhandler.enumservice.EnumPoolFramework;
import de.mint.asyncmysqlpoolhandler.mainservice.AsyncMySQLPoolHandler;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionPool implements Closeable {

    private final HikariDataSource ds;

    public MySQLConnectionPool(String url, String urldata, String user, String password) throws ClassNotFoundException, SQLException {

        String[] data = urldata.split("-");

        // returns a default configuration
        ConfigPoolFramework configPoolFramework = ConfigBuilder.getConfigBuilder().build();
        AsyncMySQLPoolHandler mySQLPoolHandler = new AsyncMySQLPoolHandler(data[0], Integer.parseInt(data[1]), user, password, data[2], EnumPoolFramework.HIKARICP, configPoolFramework);


        this.ds = mySQLPoolHandler.createHikariPool(data[2], data[0], Integer.parseInt(data[1]), user, password, configPoolFramework);

        HikariConfigMXBean hikariConfigMXBean = this.ds.getHikariConfigMXBean();

        hikariConfigMXBean.setUsername(user);
        hikariConfigMXBean.setPassword(password);
        hikariConfigMXBean.setMinimumIdle(2);

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setReadOnly(false);

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setPoolName("LogBlock-Connection-Pool");

        hikariConfig.addDataSourceProperty("useUnicode", "true");
        hikariConfig.addDataSourceProperty("characterEncoding", "utf-8");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    }

    @Override
    public void close() {
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = this.ds.getConnection();
        if (Config.mb4) {
            connection.createStatement().executeQuery("SET NAMES utf8mb4");
        }
        return connection;
    }

}
