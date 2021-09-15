package me.yarinlevi.qpunishments.commands.utilities;

import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.stream.Collectors;

public class StaffChatCommand extends Command {
    public StaffChatCommand() {
        super("staffchat", "qpunishments.commands.staffchat", "sc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            StringBuilder sb = new StringBuilder();

            for (String arg : args) {
                sb.append(arg).append(" ");
            }

            for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers().stream().filter(x ->x .hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", sender.getName(), sb.toString()));
            }
        }
    }
}
