package me.yarinlevi.qpunishments.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.history.CommentUtils;
import me.yarinlevi.qpunishments.history.PunishmentFormatUtils;
import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

public class LookupCommand implements SimpleCommand {
    static Pattern numberPattern = Pattern.compile("([0-9])+");

    @Override
    public void execute(Invocation invocation) {
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

            printLookup(sender, targetPlayer, mode, limit, debug);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.lookup");
    }

    public void printLookup(CommandSource sender, String targetPlayer, QueryMode mode, int limit, boolean debug) {
        try {
            String uuid = MojangAccountUtils.getUUID(targetPlayer);

            Component textComponent = Component.empty();
            switch (mode) {
                case ALL -> {
                    ResultSet rs = QVelocityPunishments.getInstance().getMysql().get("SELECT * FROM `playerData` WHERE `uuid`=\"" + uuid + "\"");
                    String firstLogin = MessagesUtils.getRawString("never_logged_on");
                    String lastLogin = MessagesUtils.getRawString("never_logged_on");

                    if (rs != null && rs.next()) {
                        firstLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("firstLogin"));
                        lastLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("lastLogin"));
                    }


                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_all", QVelocityPunishments.getInstance().getVersion()));

                    textComponent = textComponent.append(MessagesUtils.getMessage("first_login", firstLogin));
                    textComponent = textComponent.append(MessagesUtils.getMessage("last_login", lastLogin));

                    textComponent = textComponent.append(Component.text(" "));

                    textComponent = textComponent.append(CommentUtils.getCommentsOfMember(uuid, 3, false));
                    textComponent = textComponent.append(Component.text("\n"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestPunishmentsOfMember(uuid, 3));

                    Iterator<String> namesIterator = MojangAccountUtils.getNameHistory(uuid).listIterator();

                    Component nameHistory = MessagesUtils.getMessage("name_history");

                    while (namesIterator.hasNext()) {
                        nameHistory = nameHistory.append(MessagesUtils.getMessage("name_history_format", namesIterator.next()));

                        if (namesIterator.hasNext()) {
                            nameHistory = nameHistory.append(Component.text(", "));
                        }
                    }

                    textComponent = textComponent.append(nameHistory);
                }

                case COMMENT -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Comment"));
                    textComponent = textComponent.append(CommentUtils.getCommentsOfMember(uuid, limit, debug));
                }

                case MUTE -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Mute"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.MUTE, limit));
                }

                case BAN -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Ban"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.BAN, limit));
                }

                case KICK -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Kick"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.KICK, limit));
                }
            }

            sender.sendMessage(textComponent);

        } catch (UUIDNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        } catch (SQLException | IOException | PlayerNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    /*
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1 && !args[0].equalsIgnoreCase("-debug") || args.length == 2 && args[0].equalsIgnoreCase("-debug")) {
            list.add("comment");
            list.add("ban");
            list.add("mute");
            list.add("kick");
        }

        return list;
    }
     */
}
