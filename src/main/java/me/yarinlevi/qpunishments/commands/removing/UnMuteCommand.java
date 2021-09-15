package me.yarinlevi.qpunishments.commands.removing;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.punishments.PunishmentType;

public class UnMuteCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        CommandUtils.remove(sender, args, PunishmentType.MUTE);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.unmute");
    }
}
