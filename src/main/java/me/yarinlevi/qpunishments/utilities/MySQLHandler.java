package me.yarinlevi.qpunishments.utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import me.yarinlevi.qpunishments.exceptions.MaxConnectionsException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YarinQuapi
 */
public class MySQLHandler {
    @Getter private static MySQLHandler instance;
    private HikariConfig connectionConfig;
    @Nullable private HikariDataSource dataSource = null;
    private final int maxConnections = 5;

    private final List<Connection> connectionsList = new ArrayList<>();

    private int connections = 0;

    public MySQLHandler(Configuration config) {
        instance = this;

        if (config.getString("mysql.host") == null
                || config.getString("mysql.database") == null
                || config.getString("mysql.port") == null
                || config.getString("mysql.user") == null
                || config.getString("mysql.pass") == null) {
            System.out.println("[QMySQL] Hey! you haven't configured your mysql connection! aborting connection!");
            return;
        }

        String hostName = config.getString("mysql.host");
        String database = config.getString("mysql.database");
        int port = config.getInt("mysql.port");
        String user = config.getString("mysql.user");
        String pass = config.getString("mysql.pass");

        connectionConfig = new HikariConfig();

        //MYSQL 8.x CONNECTOR - com.mysql.cj.jdbc.MysqlDataSource
        //MYSQL 5.x CONNECTOR - com.mysql.jdbc.jdbc2.optional.MysqlDataSource

        connectionConfig.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        connectionConfig.addDataSourceProperty("serverName", hostName);
        connectionConfig.addDataSourceProperty("port", port);
        connectionConfig.addDataSourceProperty("databaseName", database);
        connectionConfig.addDataSourceProperty("user", user);
        connectionConfig.addDataSourceProperty("password", pass);
        connectionConfig.addDataSourceProperty("useSSL", config.getBoolean("mysql.ssl"));
        connectionConfig.addDataSourceProperty("autoReconnect", true);
        //dataSource.addDataSourceProperty("useUnicode", true);
        connectionConfig.addDataSourceProperty("characterEncoding", "UTF-8");

        String punishmentTableSQL = "CREATE TABLE IF NOT EXISTS `punishments`(" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`punished_uuid` VARCHAR(40) NOT NULL," +
                "`punished_by_uuid` VARCHAR(40) DEFAULT NULL," +
                "`punished_by_name` VARCHAR(16) NOT NULL," +
                "`reason` TEXT NOT NULL, " +
                "`expire_date` TEXT DEFAULT NULL," +
                "`punishment_type` TEXT NOT NULL," +
                "`server` TEXT NOT NULL," +
                "`date_added` TEXT NOT NULL," +
                "`bypass_expire_date` BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (`id`)" +
                ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";

        String proofTableSQL = "CREATE TABLE IF NOT EXISTS `comments` ("
                + "`id` INT NOT NULL AUTO_INCREMENT,"
                + "`punished_uuid` VARCHAR(40) NOT NULL,"
                + "`content` varchar(255) NOT NULL,"
                + "`punished_by_uuid` varchar(40) NOT NULL,"
                + "`punished_by_name` varchar(40) NOT NULL,"
                + "`date_added` TEXT NOT NULL,"
                + "PRIMARY KEY (`id`)"
                + ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";

        String playerDataTableSQL = "CREATE TABLE IF NOT EXISTS `playerData` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`uuid` VARCHAR(40) NOT NULL," +
                "`name` VARCHAR(16) NOT NULL," +
                "`ip` TEXT NOT NULL," +
                "`firstLogin` TEXT NOT NULL," +
                "`lastLogin` TEXT NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";


        dataSource = new HikariDataSource(connectionConfig);

        System.out.println("Please await mysql hook...");
        try {
            Connection connection = dataSource.getConnection();
            connections++;

            Statement statement = connection.createStatement();
            {
                statement.executeUpdate(punishmentTableSQL);
                statement.executeUpdate(proofTableSQL);
                statement.executeUpdate(playerDataTableSQL);
                statement.closeOnCompletion();
                System.out.println("Successfully connected to MySQL database!");
            }

            connection.close();

            connections--;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println("Something went horribly wrong while connecting to database!");
        }
    }

    @SneakyThrows
    private Connection getConnection() {
        if (connections >= maxConnections) throw new MaxConnectionsException("Reached max connections possible on the SQL connection");

        if (dataSource != null && !dataSource.isClosed()) {

            connections++;

            return dataSource.getConnection();
        } else {

            dataSource = new HikariDataSource(connectionConfig);

            connections++;

            return dataSource.getConnection();
        }
    }

    @Nullable
    public ResultSet get(String query) {
        try {
            Connection connection = getConnection();

            ResultSet set = connection.createStatement().executeQuery(query);

            connection.close();

            return set;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public int update(String query) {
        try {
            Connection connection = getConnection();

            int iii = connection.createStatement().executeUpdate(query);

            connection.close();

            return iii;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public boolean insert(String query) {
        try {
            Connection connection = getConnection();

            boolean insert = connection.createStatement().execute(query);

            connection.close();

            return insert;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean insertLarge(List<String> list) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            for (String s : list) {
                statement.addBatch(s);
            }

            statement.executeBatch();
            statement.closeOnCompletion();
            connection.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
