package me.yarinlevi.qpunishments.commands.utilities;

import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

public class ReloadMessages implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        MessagesUtils.reload();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.commands.reloadmessages");
    }
}
