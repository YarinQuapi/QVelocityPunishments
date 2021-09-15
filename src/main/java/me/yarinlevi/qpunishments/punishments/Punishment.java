package me.yarinlevi.qpunishments.punishments;

import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.TimeFormatUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * @author YarinQuapi
 */
public class Punishment {
    @Getter @Setter private int id;
    @Getter private final UUID punished_player_uuid;
    @Getter private final PunishmentType punishmentType;
    @Getter @Nullable private final UUID punished_by_player_uuid;
    @Getter private final String punished_by_name;
    @Getter private final String server;
    @Getter private final long duration;
    @Getter @Nullable private final String reason;
    @Getter private final boolean perm;
    @Getter private final boolean silent;

    public Punishment(UUID punishedPlayerUUID, PunishmentType type, @Nullable UUID punishedByPlayerUUID, String punishedByPlayerName, String server, @Nullable String reason, long duration, boolean permanent, boolean silent) {
        this.punished_player_uuid = punishedPlayerUUID;
        this.punishmentType = type;
        this.punished_by_player_uuid = punishedByPlayerUUID;
        this.punished_by_name = punishedByPlayerName;
        this.server = server;
        this.reason = reason;
        this.duration = duration;
        this.perm = permanent;
        this.silent = silent;
    }

    /*
    public void printDebug() {
        ProxyServer.getInstance().broadcast(new TextComponent("target player (uuid): " + punished_player_uuid.toString()));

        try {
            ProxyServer.getInstance().broadcast(new TextComponent("target player (name): " + MojangAccountUtils.getName(punished_player_uuid.toString().replaceAll("-", ""))));
        } catch (PlayerNotFoundException e) {
            e.printStackTrace();
        }

        ProxyServer.getInstance().broadcast(new TextComponent("type: " +  punishmentType.key));
        ProxyServer.getInstance().broadcast(new TextComponent("executed by (uuid): " + (punished_by_player_uuid != null ? punished_by_player_uuid.toString() : "No UUID (Console?)")));
        ProxyServer.getInstance().broadcast(new TextComponent("executed by (name): " + punished_by_name));
        ProxyServer.getInstance().broadcast(new TextComponent("duration: " + duration));
        ProxyServer.getInstance().broadcast(new TextComponent("permanent: " + perm));
        ProxyServer.getInstance().broadcast(new TextComponent("reason: " + (reason != null ? reason : "No reason given")));
    }
     */

    public void execute() {
        ProxiedPlayer player;
        if ((player = QBungeePunishments.getInstance().getProxy().getPlayer(punished_player_uuid)) != null) {
            switch (punishmentType) {
                case BAN -> player.disconnect(TimeFormatUtils.format("&cYou have been banned from this server\n" + reason));
                case KICK -> player.disconnect(TimeFormatUtils.format("&cYou have been kicked from this server\n" + reason));
                case MUTE -> player.sendMessage(TimeFormatUtils.format("&cYou have been muted!"));
            }
        }

        this.addToMySQL();

        if (!silent && QBungeePunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishmentType.getKey())) {
            try {
                this.broadcast();
            } catch (PlayerNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast() throws PlayerNotFoundException {
        switch (punishmentType) {
            case BAN -> {
                if (duration == 0) {
                    ProxyServer.getInstance().broadcast(MessagesUtils.getMessage("perm_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                } else {
                    ProxyServer.getInstance().broadcast(MessagesUtils.getMessage("temp_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                }
            }
            case MUTE -> {
                if (duration == 0) {
                    ProxyServer.getInstance().broadcast(MessagesUtils.getMessage("perm_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                } else {
                    ProxyServer.getInstance().broadcast(MessagesUtils.getMessage("temp_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                }
            }
            case KICK -> ProxyServer.getInstance().broadcast(MessagesUtils.getMessage("kick", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
        }
    }

    private void addToMySQL() {
        String sql;
        if (punishmentType != PunishmentType.KICK) {
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
        QBungeePunishments.getInstance().getMysql().insert(sql);
    }
}
