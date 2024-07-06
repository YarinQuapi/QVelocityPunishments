package me.yarinlevi.qpunishments.commands.administration;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

public class LockdownModeCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        QVelocityPunishments.getInstance().setLockdownMode(!QVelocityPunishments.getInstance().isLockdownMode());

        if (QVelocityPunishments.getInstance().isLockdownMode()) {
            invocation.source().sendMessage(MessagesUtils.getMessage("lockdown_enabled"));

            for (Player player : QVelocityPunishments.getInstance().getServer().getAllPlayers()) {
                if (!player.hasPermission("qpunishments.admin")) {
                    player.disconnect(MessagesUtils.getMessage("lockdown"));
                }
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.admin");
    }
}
