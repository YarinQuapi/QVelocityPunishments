package me.yarinlevi.qpunishments.support.velocity.commands.removing;

import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.universal.commands.ICommandSender;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishmentsBoot;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

public class UnIpMuteCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        ICommandSender sender = QVelocityPunishmentsBoot.getInstance().getVelocitySourceWrapper().wrap(invocation.source());
        String[] args = invocation.arguments();

        try {
            RemoverCommandUtils.remove(sender, args, PunishmentType.MUTE, true);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.unipmute");
    }
}
