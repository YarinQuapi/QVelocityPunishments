package me.yarinlevi.qpunishments.utilities;

import lombok.Getter;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisHandler {
    @Getter private static boolean redis = false;

    @Getter private final Jedis jedis;


    String hostName;
    int port;
    String pass;

    public RedisHandler(Configuration config) {
        QVelocityPunishments.getInstance().getLogger().warning("0");

        hostName = config.getString("redis.host");
        port = config.getInt("redis.port");
        pass = config.getString("redis.pass");

        QVelocityPunishments.getInstance().getLogger().warning(hostName);
        QVelocityPunishments.getInstance().getLogger().warning(port + "");
        QVelocityPunishments.getInstance().getLogger().warning(pass);


        redis = true;


        jedis = new Jedis(hostName, port);
        jedis.auth(pass);
        jedis.connect();


        QVelocityPunishments.getInstance().getServer().getScheduler().buildTask(QVelocityPunishments.getInstance(), this::subscribePunishmentListener).schedule();

        System.out.println("[QRedis v0.1A] enabled! please report any bugs! :D");
    }

    public void postPunishment(Punishment punishment) {
        jedis.publish("qpunishments-" + QVelocityPunishments.getInstance().getVersion(), punishment.toString());
    }

    protected void subscribePunishmentListener() {
        Jedis jsub = new Jedis(hostName, port);
        jsub.auth(pass);
        jsub.connect();

        jsub.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("qpunishments-" + QVelocityPunishments.getInstance().getVersion())) {
                    Punishment punishment = Punishment.fromString(message);
                    punishment.execute(true);
                }
            }
        }, "qpunishments-" + QVelocityPunishments.getInstance().getVersion());
    }
}