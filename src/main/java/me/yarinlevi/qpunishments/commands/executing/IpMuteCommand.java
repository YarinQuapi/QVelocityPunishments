package me.yarinlevi.qpunishments.commands.executing;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.NotEnoughArgumentsException;
import me.yarinlevi.qpunishments.exceptions.NotValidIpException;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.ServerNotExistException;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.punishments.PunishmentBuilder;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.punishments.PunishmentUtils;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

/**
 * @author YarinQuapi
 */
public class IpMuteCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            PunishmentBuilder punishmentBuilder;

            try {
                punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.MUTE, true);
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

            Punishment pun = punishmentBuilder.build();
            pun.execute();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.ipmute");
    }
}
