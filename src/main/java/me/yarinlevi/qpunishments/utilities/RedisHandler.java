package me.yarinlevi.qpunishments.utilities;

import lombok.Getter;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisHandler {
    @Getter private static boolean redis = false;

    @Getter private final Jedis jedis;

    public RedisHandler(Configuration config) throws Exception {
        if (config.getString("redis.host") == null
                || config.getString("redis.port") == null
                || config.getString("redis.pass") == null) {
            throw new Exception();
        }

        String hostName = config.getString("redis.host");
        int port = config.getInt("redis.port");
        //String user = config.getString("redis.user");
        String pass = config.getString("redis.pass");

        redis = true;

        jedis = new Jedis(hostName, port);

        jedis.auth(pass);
        jedis.connect();

        this.subscribePunishmentListener();

        System.out.println("[QRedis v0.1A] enabled! please report any bugs! :D");
    }

    public void postPunishment(Punishment punishment) {
        jedis.publish("qpunishments-" + QVelocityPunishments.getInstance().getVersion(), punishment.toString());
    }

    private void subscribePunishmentListener() {
        Jedis punishmentListener = new Jedis();
        punishmentListener.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("qpunishments-" + QVelocityPunishments.getInstance().getVersion())) {
                    Punishment punishment = Punishment.fromString(message);

                    punishment.execute(true);
                }
            }
        });
    }
}