package me.yarinlevi.qpunishments.commands.utilities;

import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class FindCommand extends Command {
    public FindCommand() {
        super("find", "qpunishments.command.find", "locate");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            String playerName = args[0];

            ProxyServer.getInstance().getPlayers().stream().filter(x -> x.getName().equalsIgnoreCase(playerName)).findAny()
                    .ifPresentOrElse(proxiedPlayer ->
                            sender.sendMessage(MessagesUtils.getMessage("find_command", proxiedPlayer.getName(), proxiedPlayer.getServer().getInfo().getName())),
                            () -> sender.sendMessage(MessagesUtils.getMessage("player_not_online")));
        }
    }
}
