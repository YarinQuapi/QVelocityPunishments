package me.yarinlevi.qpunishments.support.velocity.commands.utilities;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishmentsBoot;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class CommentCommand implements SimpleCommand {
    Pattern pattern = Pattern.compile("([A-z0-9])\\w+");

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {

            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].trim();
            }

            if (args.length > 1) {
                if (args[0].length() >= 3 && args[0].length() <= 16 && pattern.matcher(args[0]).matches()) {
                    try {
                        String uuid = MojangAccountUtils.getUUID(args[0]);

                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }

                        String senderUUID = null;
                        String senderName = "Console";

                        if (sender instanceof Player proxiedPlayer) {
                            senderUUID = proxiedPlayer.getUniqueId().toString();
                            senderName = proxiedPlayer.getUsername();
                        }


                        QVelocityPunishmentsBoot.getInstance().getMysql()
                                .insert(String.format("INSERT INTO `comments` (`punished_uuid`, `content`, `punished_by_uuid`, `punished_by_name`, `date_added`) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\")",
                                        uuid, sb, senderUUID, senderName, System.currentTimeMillis()));


                        sender.sendMessage(MessagesUtils.getMessage("comment_added", args[0]));
                    } catch (IOException | UUIDNotFoundException e) {
                        sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                    }
                } else {
                    sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                }
            } else {
                sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.comment");
    }
}
