package me.yarinlevi.qpunishments.punishments;

import lombok.Getter;
import me.yarinlevi.qpunishments.exceptions.ServerNotExistException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * @author YarinQuapi
 */
public class PunishmentBuilder {
    private final UUID targetPlayerUUID;
    private PunishmentType type;
    @Nullable private UUID moderatorUUID = null;
    private String moderatorName;
    private long duration;
    private boolean permanent;
    private String server = "global";
    @Nullable private String reason;
    @Getter private boolean silent;

    public PunishmentBuilder(UUID targetUUID) {
        this.targetPlayerUUID = targetUUID;
    }

    /**
     * Set punishment type
     * @param type Type
     * @return Builder
     */
    public PunishmentBuilder setPunishmentType(PunishmentType type) {
        this.type = type;

        return this;
    }

    /**
     * Set the executor's uuid
     * @param moderatorUUID UUID
     * @return Builder
     */
    public PunishmentBuilder setModeratorUUID(@Nullable UUID moderatorUUID) {
        this.moderatorUUID = moderatorUUID;

        return this;
    }

    public PunishmentBuilder setServer(String serverName) throws ServerNotExistException {
        if (!Objects.equals(serverName, "global") && !QBungeePunishments.getInstance().getProxy().getServers().containsKey(serverName)) throw new ServerNotExistException();

        this.server = serverName;

        return this;
    }

    public PunishmentBuilder setModeratorName(String moderatorName) {
        this.moderatorName = moderatorName;

        return this;
    }

    public PunishmentBuilder setDuration(long duration) {
        this.duration = duration;

        return this;
    }

    public PunishmentBuilder setPermanent(boolean permanent) {
        this.permanent = permanent;

        if (permanent) this.duration = 0;

        return this;
    }

    public PunishmentBuilder setReason(@Nullable String reason) {
        this.reason = reason;

        return this;
    }

    public PunishmentBuilder setSilent(boolean silent) {
        this.silent = silent;

        return this;
    }

    public Punishment build() {
        return new Punishment(
                targetPlayerUUID,
                type,
                moderatorUUID,
                moderatorName,
                server,
                reason,
                duration,
                permanent,
                silent);
    }
}
