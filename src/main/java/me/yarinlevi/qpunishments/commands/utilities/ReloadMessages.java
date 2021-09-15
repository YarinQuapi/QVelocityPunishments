package me.yarinlevi.qpunishments.commands.utilities;

import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class ReloadMessages extends Command {
    public ReloadMessages() {
        super("reloadmessages", "qpunishments.commands.reloadmessages");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesUtils.reload();
    }
}
