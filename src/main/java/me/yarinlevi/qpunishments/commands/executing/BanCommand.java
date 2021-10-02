package me.yarinlevi.qpunishments.commands.executing;

import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.common.abstraction.player.proxy.ICommandSender;
import me.yarinlevi.qpunishments.exceptions.*;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.punishments.PunishmentBuilder;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.punishments.PunishmentUtils;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.sql.SQLException;

/**
 * @author YarinQuapi
 */
public class BanCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        ICommandSender sender = QVelocityPunishments.getInstance().getVelocitySourceWrapper().wrap(invocation.source());
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            PunishmentBuilder punishmentBuilder;

            try {
                punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.BAN, false);
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

                pun.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (PlayerPunishedException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_punished"));
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.ban");
    }
}
