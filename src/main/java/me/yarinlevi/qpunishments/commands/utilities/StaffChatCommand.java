package me.yarinlevi.qpunishments.commands.utilities;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.util.stream.Collectors;

public class StaffChatCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            StringBuilder sb = new StringBuilder();

            for (String arg : args) {
                sb.append(arg).append(" ");
            }

            if (sender instanceof Player player) {
                for (Player proxiedPlayer : QVelocityPunishments.getInstance().getServer().getAllPlayers().stream().filter(x -> x.hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                    proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", player.getUsername(), sb.toString()));
                }
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.commands.staffchat");
    }
}
