package me.yarinlevi.qpunishments.commands;

import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.history.CommentUtils;
import me.yarinlevi.qpunishments.history.PunishmentFormatUtils;
import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class LookupCommand extends Command implements TabExecutor {
    static Pattern numberPattern = Pattern.compile("([0-9])+");

    public LookupCommand() {
        super("lookup", "qpunishments.command.lookup");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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

    public void printLookup(CommandSender sender, String targetPlayer, QueryMode mode, int limit, boolean debug) {
        try {
            String uuid = MojangAccountUtils.getUUID(targetPlayer);

            TextComponent textComponent = new TextComponent();
            switch (mode) {
                case ALL -> {
                    ResultSet rs = QBungeePunishments.getInstance().getMysql().get("SELECT * FROM `playerData` WHERE `uuid`=\"" + uuid + "\"");
                    String firstLogin = MessagesUtils.getRawString("never_logged_on");
                    String lastLogin = MessagesUtils.getRawString("never_logged_on");

                    if (rs != null && rs.next()) {
                        firstLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("firstLogin"));
                        lastLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("lastLogin"));
                    }


                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_all", QBungeePunishments.getInstance().getDescription().getVersion()));

                    textComponent.addExtra(MessagesUtils.getMessage("first_login", firstLogin));
                    textComponent.addExtra(MessagesUtils.getMessage("last_login", lastLogin));

                    textComponent.addExtra(" ");

                    textComponent.addExtra(CommentUtils.getCommentsOfMember(uuid, 3, false));
                    textComponent.addExtra("\n");
                    textComponent.addExtra(PunishmentFormatUtils.getLatestPunishmentsOfMember(uuid, 3));

                    Iterator<String> namesIterator = MojangAccountUtils.getNameHistory(uuid).listIterator();

                    TextComponent nameHistory = MessagesUtils.getMessage("name_history");

                    while (namesIterator.hasNext()) {
                        nameHistory.addExtra(MessagesUtils.getMessage("name_history_format", namesIterator.next()));

                        if (namesIterator.hasNext()) {
                            nameHistory.addExtra(", ");
                        }
                    }

                    textComponent.addExtra(nameHistory);
                }

                case COMMENT -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getDescription().getVersion(), "Comment"));
                    textComponent.addExtra(CommentUtils.getCommentsOfMember(uuid, limit, debug));
                }

                case MUTE -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getDescription().getVersion(), "Mute"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.MUTE, limit));
                }

                case BAN -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getDescription().getVersion(), "Ban"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.BAN, limit));
                }

                case KICK -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getDescription().getVersion(), "Kick"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuid, PunishmentType.KICK, limit));
                }
            }

            sender.sendMessage(textComponent);

        } catch (UUIDNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        } catch (SQLException | IOException | PlayerNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

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
}
