package me.yarinlevi.qpunishments.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public class LookupIpCommand implements SimpleCommand {
    static Pattern numberPattern = Pattern.compile("([0-9])+");

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 0) {
            QueryMode mode;

            int limit = 3;
            int limitArgPos = 2;
            int modeArgPos = 1;
            boolean debug = false;

            String targetPlayer;

            if (args[0].equalsIgnoreCase("-debug")) {
                debug = true;
                targetPlayer = args[1];
                modeArgPos++;
                limitArgPos++;
            } else {
                targetPlayer = args[0];
            }

            if (args.length >= 2) {
                if (args.length == 3 && limitArgPos == 2 || args.length == 4 && limitArgPos == 3) {
                    if (numberPattern.matcher(args[limitArgPos]).matches()) {
                        limit = Integer.parseInt(args[limitArgPos]);
                    } else {
                        sender.sendMessage(MessagesUtils.getMessage("invalid_limit"));
                    }
                }

                final int pos = modeArgPos;

                mode = Arrays.stream(QueryMode.values()).anyMatch(x -> x.getKey().toLowerCase().startsWith(args[pos].toLowerCase()))
                        ? Arrays.stream(QueryMode.values()).filter(x -> x.getKey().toLowerCase().startsWith(args[pos].toLowerCase())).findFirst().get()
                        : QueryMode.ALL;

                //mode = QueryMode.valueOf(args[modeArgPos].toUpperCase());
            } else {
                mode = QueryMode.ALL;
            }

            LookupShared.printLookup(sender, targetPlayer, mode, limit, debug, true);
        }
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.lookupip");
    }
}
