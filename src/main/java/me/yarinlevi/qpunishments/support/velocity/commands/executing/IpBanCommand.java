package me.yarinlevi.qpunishments.support.velocity.commands.executing;

import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.*;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.punishments.PunishmentBuilder;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.punishments.PunishmentUtils;
import me.yarinlevi.qpunishments.support.universal.commands.ICommandSender;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishmentsBoot;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.sql.SQLException;

/**
 * @author YarinQuapi
 */
public class IpBanCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        ICommandSender sender = QVelocityPunishmentsBoot.getInstance().getVelocitySourceWrapper().wrap(invocation.source());
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            PunishmentBuilder punishmentBuilder;

            try {
                punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.BAN, true);
            } catch (PlayerNotFoundException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                return;
            } catch (NotEnoughArgumentsException e) {
                sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                return;
            } catch (ServerNotExistException e) {
                sender.sendMessage(MessagesUtils.getMessage("server_not_found"));
                return;
            } catch (NotValidIpException e) {
                sender.sendMessage(MessagesUtils.getMessage("invalid_ip_address"));
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
        return invocation.source().hasPermission("qpunishments.command.ipban");
    }
}
