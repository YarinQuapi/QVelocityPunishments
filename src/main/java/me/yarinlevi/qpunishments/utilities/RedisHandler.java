package me.yarinlevi.qpunishments.utilities;

import lombok.Getter;
import me.yarinlevi.qpunishments.exceptions.PlayerPunishedException;
import me.yarinlevi.qpunishments.exceptions.ServerNotExistException;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RedisHandler {
    @Getter private static boolean redis = false;

    @Getter private final Jedis jedis;
    @Getter private final Set<UUID> sentIds = new HashSet<>();

    String hostName;
    int port;
    String pass;

    public RedisHandler(Configuration config) {
        hostName = config.getString("redis.host");
        port = config.getInt("redis.port");
        pass = config.getString("redis.pass");
        redis = true;

        jedis = new Jedis(hostName, port);
        jedis.auth(pass);
        jedis.connect();

        QVelocityPunishments.getInstance().getServer().getScheduler().buildTask(QVelocityPunishments.getInstance(), this::subscribePunishmentListener).schedule();

        System.out.println("[QRedis v0.1B] enabled! please report any bugs! :D");
    }

    public void postPunishment(Punishment punishment) {
        String string = punishment.toString();
        UUID id = UUID.fromString(string.split("@")[0].split("id=")[1]);
        this.sentIds.add(id);

        jedis.publish("qpunishments-" + QVelocityPunishments.getInstance().getVersion(), string);
    }

    protected void subscribePunishmentListener() {
        Jedis jSub = new Jedis(hostName, port);
        jSub.auth(pass);
        jSub.connect();

        jSub.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("qpunishments-" + QVelocityPunishments.getInstance().getVersion())) {
                    Punishment punishment;
                    try {
                        punishment = Punishment.fromString(message);

                        if (!sentIds.contains(punishment.getRedisUUID())) {
                            punishment.execute(true);
                        }
                    } catch (ServerNotExistException | PlayerPunishedException | SQLException ignored) { }

                }
            }
        }, "qpunishments-" + QVelocityPunishments.getInstance().getVersion());
    }
}