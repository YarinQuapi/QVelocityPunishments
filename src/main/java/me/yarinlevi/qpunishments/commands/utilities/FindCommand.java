package me.yarinlevi.qpunishments.commands.utilities;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

public class FindCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            String playerName = args[0];

            QVelocityPunishments.getInstance().getServer().getAllPlayers().stream().filter(x -> x.getUsername().equalsIgnoreCase(playerName)).findAny()
                    .ifPresentOrElse(proxiedPlayer ->
                            sender.sendMessage(MessagesUtils.getMessage("find_command", proxiedPlayer.getUsername(), proxiedPlayer.getCurrentServer().orElseThrow().getServerInfo().getName())),
                            () -> sender.sendMessage(MessagesUtils.getMessage("player_not_online")));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.commands.find");
    }
}
