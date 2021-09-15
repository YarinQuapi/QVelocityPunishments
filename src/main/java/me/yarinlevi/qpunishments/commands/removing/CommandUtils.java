package me.yarinlevi.qpunishments.commands.removing;

import com.velocitypowered.api.command.CommandSource;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.Utilities;

import java.io.IOException;

public class CommandUtils {
    public static void remove(CommandSource sender, String[] args, PunishmentType type) {
        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            String playerName;
            boolean silent = false;

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("-s")) {
                    sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                    return;
                } else {
                    playerName = args[0];
                }
            } else {
                if (args[0].equalsIgnoreCase("-s")) {
                    silent = true;
                    playerName = args[1];
                } else {
                    playerName = args[0];
                }
            }

            try {
                String uuid = MojangAccountUtils.getUUID(playerName);
                String punishment = type.getKey();

                String sql = String.format("UPDATE `punishments` SET `bypass_expire_date`=true WHERE `punished_uuid` = \"%s\" AND `expire_date` > \"%s\" AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" OR `punished_uuid` = \"%s\" AND `expire_date`=0 AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" ORDER BY id DESC;",
                        uuid, System.currentTimeMillis(), punishment, uuid, punishment);

                if (QVelocityPunishments.getInstance().getMysql().update(sql) >= 1) {
                    sender.sendMessage(MessagesUtils.getMessage("pardon_successful", playerName, punishment));

                    if (!silent && QVelocityPunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishment)) {
                        Utilities.broadcast(MessagesUtils.getMessage("un" + type.getKey().toLowerCase(), playerName));
                    }

                } else {
                    sender.sendMessage(MessagesUtils.getMessage("no_punishment_found"));
                }

            } catch (UUIDNotFoundException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
