package me.yarinlevi.qpunishments.migrators;

import com.velocitypowered.api.proxy.Player;
import com.zaxxer.hikari.HikariDataSource;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.utilities.Configuration;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LiteBansMigrator {
    private Connection connection;

    public LiteBansMigrator(Configuration config) {
        if (config.getString("mysql.host") == null
                || config.getString("mysql.database") == null
                || config.getString("mysql.port") == null
                || config.getString("mysql.user") == null
                || config.getString("mysql.pass") == null) {
            System.out.println("[QMySQLMigrator] Migration MySQL connection not configured correctly.");
            return;
        }

        String hostName = config.getString("mysql.host");
        String database = config.getString("mysql.database");
        int port = config.getInt("mysql.port");
        String user = config.getString("mysql.user");
        String pass = config.getString("mysql.pass");

        HikariDataSource dataSource = new HikariDataSource();

        //MYSQL 8.x CONNECTOR - com.mysql.cj.jdbc.MysqlDataSource
        //MYSQL 5.x CONNECTOR - com.mysql.jdbc.jdbc2.optional.MysqlDataSource

        dataSource.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        dataSource.addDataSourceProperty("serverName", hostName);
        dataSource.addDataSourceProperty("port", port);
        dataSource.addDataSourceProperty("databaseName", database);
        dataSource.addDataSourceProperty("user", user);
        dataSource.addDataSourceProperty("password", pass);
        dataSource.addDataSourceProperty("useSSL", config.getBoolean("mysql.ssl"));
        dataSource.addDataSourceProperty("autoReconnect", true);
        dataSource.addDataSourceProperty("useUnicode", true);
        dataSource.addDataSourceProperty("characterEncoding", "UTF-8");

        System.out.println("Please await mysql hook...");
        try {
            connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println("Something went horribly wrong while connecting to database!");
        }


        QVelocityPunishments.getInstance().getLogger().warning("MIGRATING BAN TABLE NOW...");
        migrateBanTable();
        QVelocityPunishments.getInstance().getLogger().warning("FINISHED MIGRATING BAN TABLE!");
        QVelocityPunishments.getInstance().getLogger().warning("MIGRATING MUTE TABLE NOW...");
        migrateMuteTable();
        QVelocityPunishments.getInstance().getLogger().warning("FINISHED MIGRATING MUTE TABLE!");
        QVelocityPunishments.getInstance().getLogger().warning("MIGRATING KICK TABLE NOW...");
        migrateKickTable();
    }

    @Nullable
    public ResultSet get(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private void migrateBanTable() {
        String sql = "SELECT * FROM `litebans_bans`;";
        ResultSet rs = get(sql);

        List<String> queue = analyzeTable(PunishmentType.BAN, rs);

        QVelocityPunishments.getInstance().getMysql().insertLarge(queue);
    }

    private void migrateMuteTable() {
        String sql = "SELECT * FROM `litebans_mutes`;";
        ResultSet rs = get(sql);

        List<String> queue = analyzeTable(PunishmentType.MUTE, rs);

        QVelocityPunishments.getInstance().getMysql().insertLarge(queue);
    }

    private void migrateKickTable() {
        String sql = "SELECT * FROM `litebans_bans`;";
        ResultSet rs = get(sql);

        List<String> queue = analyzeTable(PunishmentType.KICK, rs);

        QVelocityPunishments.getInstance().getMysql().insertLarge(queue);
    }

    private List<String> analyzeTable(PunishmentType type, ResultSet rs) {
        try {
            if (rs != null && rs.next()) {
                List<String> sqlQueue = new ArrayList<>();

                do {
                    int id = rs.getInt("id");
                    String punished_uuid = rs.getString("uuid");
                    String reason = rs.getString("reason");
                    String added = rs.getString("time");
                    String time = rs.getString("until");
                    String banned_by_uuid = rs.getString("banned_by_uuid");
                    String banned_by_name = rs.getString("banned_by_name");
                    String server = rs.getString("server_scope");

                    if (rs.getBoolean("ipban")) {
                        String ipAddress = rs.getString("ip");
                        sqlQueue.add(this.determineMySQL(type, true, id, ipAddress, punished_uuid, banned_by_uuid, banned_by_name, time, reason, added, server));
                    } else {
                        sqlQueue.add(this.determineMySQL(type, false, id, "null", punished_uuid, banned_by_uuid, banned_by_name, time, reason, added, server));
                    }

                } while (rs.next());

                return sqlQueue;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private String determineMySQL(PunishmentType punishmentType, boolean ipPunishment, int id, String rawIpAddress, String punished_player_uuid, String punished_by_player_uuid, String punished_by_name, String duration, String reason, String date_added, String server) {
        String sql;
        if (punishmentType != PunishmentType.KICK) {
            if (ipPunishment) {
                sql = String.format("INSERT INTO `punishments`(`id`,`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        id,
                        rawIpAddress,
                        punished_by_player_uuid,
                        punished_by_name,
                        duration,
                        reason,
                        punishmentType.getKey(),
                        date_added,
                        server
                );
            } else {
                sql = String.format("INSERT INTO `punishments`(`id`, `punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        id,
                        punished_player_uuid,
                        punished_by_player_uuid,
                        punished_by_name,
                        duration,
                        reason,
                        punishmentType.getKey(),
                        date_added,
                        server
                );
            }
        } else {
            sql = String.format("INSERT INTO `punishments`(`id`, `punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                            "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                    id,
                    punished_player_uuid,
                    punished_by_player_uuid,
                    punished_by_name,
                    date_added,
                    reason,
                    punishmentType.getKey(),
                    date_added,
                    "global"
            );

        }
        return sql;
    }
}
