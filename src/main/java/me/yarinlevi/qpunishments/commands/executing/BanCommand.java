package me.yarinlevi.qpunishments.commands.executing;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.*;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.punishments.PunishmentBuilder;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.punishments.PunishmentUtils;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.Utilities;

import java.sql.SQLException;

/**
 * @author YarinQuapi
 */
public class BanCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            PunishmentBuilder punishmentBuilder;

            try {
                if (Utilities.validIP(args[1])) {
                    punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.BAN, true);
                } else {
                    punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.BAN, false);
                }
            } catch (PlayerNotFoundException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                return;
            } catch (NotEnoughArgumentsException e) {
                sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                return;
            } catch (ServerNotExistException e) {
                sender.sendMessage(MessagesUtils.getMessage("server_not_found"));
                return;
            } catch (NotValidIpException ignored) {
                return;
            }

            try {
                Punishment pun = punishmentBuilder.build();

                pun.execute(false);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (PlayerPunishedException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_punished"));
            }
        }
    }
}
