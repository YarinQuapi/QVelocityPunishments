package me.yarinlevi.qpunishments.support.velocity.commands.utilities;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class HistoryCommand implements SimpleCommand {
    Pattern numberPattern = Pattern.compile("([0-9])+");

    //Todo: Completely rewrite to allow for adding commands more easily and support for multiple server-systems

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            if (args.length >= 2) {
                if  (numberPattern.matcher(args[1]).matches()) {
                    int id = Integer.parseInt(args[1]);

                    switch (args[0].toLowerCase()) {
                        case "editcomment", "ec" -> {
                            if (args.length >= 3) {
                                StringBuilder sb = new StringBuilder();

                                for (int i = 2; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }

                                String sql = "UPDATE `comment` SET `content`=\"" + sb + "\"  WHERE `id`=" + id;

                                if (QVelocityPunishments.getInstance().getMysql().update(sql) != 0) {
                                    sender.sendMessage(MessagesUtils.getMessage("comment_edit_successful", id));
                                } else sender.sendMessage(MessagesUtils.getMessage("action_unsuccessful"));
                            } else sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                        }

                        case "removecomment", "rc" -> {
                            String sql = "DELETE FROM `comment` WHERE `id`=" + id;

                            if (QVelocityPunishments.getInstance().getMysql().update(sql) != 0) {
                                sender.sendMessage(MessagesUtils.getMessage("comment_removal_successful", id));
                            } else sender.sendMessage(MessagesUtils.getMessage("action_unsuccessful"));
                        }

                        case "removepunishment", "rp" -> {
                            String sql = "DELETE FROM `punishments` WHERE `id`=" + id;

                            if (QVelocityPunishments.getInstance().getMysql().update(sql) != 0) {
                                sender.sendMessage(MessagesUtils.getMessage("punishment_removal_successful", id));
                            } else sender.sendMessage(MessagesUtils.getMessage("action_unsuccessful"));
                        }

                        case "retrievecomment", "getcomment", "gc" -> {
                            String sql = "SELECT * FROM `comment` WHERE `id`=" + id;

                            ResultSet rs = QVelocityPunishments.getInstance().getMysql().get(sql);

                            try {
                                if (rs != null && rs.next()) {
                                    sender.sendMessage(MessagesUtils.getMessageWithClickable("comment_format_debug",
                                            rs.getString("content"),
                                            MojangAccountUtils.getName(rs.getString("punished_by_uuid")),
                                            rs.getString("punished_by_name"),
                                            new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("date_added")),
                                            rs.getInt("id")));

                                } else sender.sendMessage(MessagesUtils.getMessage("action_unsuccessful"));
                            } catch (SQLException | PlayerNotFoundException e) {
                                sender.sendMessage(MessagesUtils.getMessage("action_unsuccessful"));
                            }
                        }
                        default -> sender.sendMessage(MessagesUtils.getMessage("command_not_found"));
                    }
                }
            } else sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.history");
    }

    /*
    public Iterable<String> onTabComplete(CommandSource sender, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 0) {
            list.add("editcomment");
            list.add("removecomment");
            list.add("removepunishment");
            list.add("getcomment");
        }

        return list;
    }
     */
}