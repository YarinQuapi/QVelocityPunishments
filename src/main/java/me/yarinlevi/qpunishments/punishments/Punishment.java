package me.yarinlevi.qpunishments.punishments;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author YarinQuapi
 */
public class Punishment {
    @Getter @Setter private int id;
    @Getter private final boolean ipPunishment;
    @Getter private String rawIpAddress;
    @Getter @Nullable private UUID punished_player_uuid;
    @Getter private final PunishmentType punishmentType;
    @Getter @Nullable private final UUID punished_by_player_uuid;
    @Getter private final String punished_by_name;
    @Getter private final String server;
    @Getter private final long duration;
    @Getter @Nullable private final String reason;
    @Getter private final boolean perm;
    @Getter private final boolean silent;

    public Punishment(String punishedPlayerUUIDOrIp, PunishmentType type, @Nullable UUID punishedByPlayerUUID, String punishedByPlayerName, String server, @Nullable String reason, long duration, boolean permanent, boolean silent, boolean ipPunishment) {
        if (ipPunishment) {
            this.rawIpAddress = punishedPlayerUUIDOrIp;
        } else {
            this.punished_player_uuid = UUID.fromString(punishedPlayerUUIDOrIp);
        }

        this.punishmentType = type;
        this.punished_by_player_uuid = punishedByPlayerUUID;
        this.punished_by_name = punishedByPlayerName;
        this.server = server;
        this.reason = reason;
        this.duration = duration;
        this.perm = permanent;
        this.silent = silent;
        this.ipPunishment = ipPunishment;
    }

    public void execute() {
        if (!ipPunishment && QVelocityPunishments.getInstance().getServer().getPlayer(punished_player_uuid).isPresent()) {
            Player player = QVelocityPunishments.getInstance().getServer().getPlayer(punished_player_uuid).orElseThrow();

            switch (punishmentType) {
                case BAN -> player.disconnect(MessagesUtils.getMessage("you_have_been_banned"));
                case KICK -> player.disconnect(MessagesUtils.getMessage("you_have_been_kicked"));
                case MUTE -> player.sendMessage(MessagesUtils.getMessage("you_have_been_muted"));
            }
        }

        if (ipPunishment) {
            this.addToMySQL();

            List<String> sqlQueue = new ArrayList<>();

            List<Player> executedPlayers = new ArrayList<>();

            for (Player player : QVelocityPunishments.getInstance().getServer().getAllPlayers().stream().filter(x -> x.getRemoteAddress().getAddress().getHostAddress().equals(rawIpAddress)).collect(Collectors.toList())) {
                sqlQueue.add(this.addToExecuteQueue(player.getUniqueId()));

                executedPlayers.add(player);
            }

            QVelocityPunishments.getInstance().getServer().getConsoleCommandSource().sendMessage(Component.text(sqlQueue.toString()));

            if (!sqlQueue.isEmpty()) {
                QVelocityPunishments.getInstance().getMysql().insertLarge(sqlQueue);
            }

            for (Player player : executedPlayers) {
                if (player.isActive()) {
                    switch (punishmentType) {
                        case BAN -> player.disconnect(MessagesUtils.getMessage("you_have_been_banned"));
                        case MUTE -> player.sendMessage(MessagesUtils.getMessage("you_have_been_muted"));
                    }
                }
            }
        } else {
            this.addToMySQL();
        }

        if (!silent && QVelocityPunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishmentType.getKey())) {
            try {
                this.broadcast();
            } catch (PlayerNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast() throws PlayerNotFoundException {
        if (!ipPunishment) {
            if (punished_player_uuid != null) {
                switch (punishmentType) {
                    case BAN -> {
                        if (duration == 0) {
                            Utilities.broadcast(MessagesUtils.getMessage("perm_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                        } else {
                            Utilities.broadcast(MessagesUtils.getMessage("temp_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                        }
                    }
                    case MUTE -> {
                        if (duration == 0) {
                            Utilities.broadcast(MessagesUtils.getMessage("perm_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                        } else {
                            Utilities.broadcast(MessagesUtils.getMessage("temp_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                        }
                    }
                    case KICK -> Utilities.broadcast(MessagesUtils.getMessage("kick", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                }
            }
        } else {
            switch (punishmentType) {
                case BAN -> {
                    if (duration == 0) {
                        Utilities.broadcast(MessagesUtils.getMessage("perm_ip_ban", reason));
                    } else {
                        Utilities.broadcast(MessagesUtils.getMessage("temp_ip_ban", reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                    }
                }
                case MUTE -> {
                    if (duration == 0) {
                        Utilities.broadcast(MessagesUtils.getMessage("perm_ip_mute", reason));
                    } else {
                        Utilities.broadcast(MessagesUtils.getMessage("temp_ip_mute", reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                    }
                }
            }
        }
    }

    private void addToMySQL() {
        String sql;
        if (punishmentType != PunishmentType.KICK) {
            if (ipPunishment) {
                sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        this.rawIpAddress,
                        this.punished_by_player_uuid,
                        this.punished_by_name,
                        this.duration,
                        this.reason,
                        this.punishmentType.getKey(),
                        System.currentTimeMillis(),
                        this.server
                );
            } else {
                sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        this.punished_player_uuid,
                        this.punished_by_player_uuid,
                        this.punished_by_name,
                        this.duration,
                        this.reason,
                        this.punishmentType.getKey(),
                        System.currentTimeMillis(),
                        this.server
                );
            }
        } else {
            sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                            "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                    this.punished_player_uuid,
                    this.punished_by_player_uuid,
                    this.punished_by_name,
                    System.currentTimeMillis(),
                    this.reason,
                    this.punishmentType.getKey(),
                    System.currentTimeMillis(),
                    "global"
            );

        }
        QVelocityPunishments.getInstance().getMysql().insert(sql);
    }

    /**
     * Specifically for Ip Punishments!
     * @param uuid to add to queue
     */
    private String addToExecuteQueue(UUID uuid) {
        return String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                        "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                uuid,
                this.punished_by_player_uuid,
                this.punished_by_name,
                this.duration,
                this.reason,
                this.punishmentType.getKey(),
                System.currentTimeMillis(),
                this.server);
    }
}
