package me.yarinlevi.qpunishments.punishments;

import me.yarinlevi.qpunishments.exceptions.*;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.TimeFormatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.util.UUID;

/**
 * @author YarinQuapi
 */
public class PunishmentUtils {
    public static PunishmentBuilder createPunishmentBuilder(CommandSender sender, String[] args, PunishmentType type) throws PlayerNotFoundException, NotEnoughArgumentsException, ServerNotExistException {
        UUID executorUUID = null;
        String executorName;

        if (sender instanceof ProxiedPlayer proxiedPlayer) {
            executorUUID = proxiedPlayer.getUniqueId();
            executorName = proxiedPlayer.getName();
        } else {
            executorName = "Console";
        }


        int durationArgPos = 1;
        int reasonArgPos = 1;

        long duration = 0;

        boolean silent = false;
        boolean perm = false;

        String playerName;
        String serverName = "global";
        String reason;
        String durationString;

        // Name & silent construction
        if (args[0].equals("-s")) {
            durationArgPos++;
            reasonArgPos++;

            silent = true;

            if (args.length > 1) {
                playerName = args[1];
            } else {
                throw new NotEnoughArgumentsException();
            }
        } else {
            playerName = args[0];
        }

        // Duration construction
        if (args.length > durationArgPos) {
            durationString = args[durationArgPos];

            try {
                duration = TimeFormatUtils.parseDuration(durationString);
                reasonArgPos++;
            } catch (TimeNotDetectedException e) {
                perm = true;
            }
        } else {
            perm = true;
        }

        // Reason Construction and server detection
        if (args.length > reasonArgPos) {

            if (args[reasonArgPos].startsWith("server:")) {
                reasonArgPos++;
                serverName = args[reasonArgPos-1].split(":")[1];
            }

            StringBuilder builder = new StringBuilder();

            for (int j = reasonArgPos; j < args.length; j++) {
                builder.append(args[j]).append(" ");
            }

            reason = builder.toString();
        } else {
            reason = "No reason given";
        }

        String playerDashUUID;

        try {
            playerDashUUID = MojangAccountUtils.getUUID(playerName);
        } catch (UUIDNotFoundException | IOException e) {
            throw new PlayerNotFoundException();
        }

        return new PunishmentBuilder(UUID.fromString(playerDashUUID))
                .setPunishmentType(type)
                .setModeratorName(executorName)
                .setModeratorUUID(executorUUID)
                .setServer(serverName)
                .setDuration(duration)
                .setPermanent(perm)
                .setReason(reason)
                .setSilent(silent);
    }
}
