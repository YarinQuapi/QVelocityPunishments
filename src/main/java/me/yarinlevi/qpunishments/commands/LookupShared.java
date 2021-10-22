package me.yarinlevi.qpunishments.commands;

import com.velocitypowered.api.command.CommandSource;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.history.CommentUtils;
import me.yarinlevi.qpunishments.history.PunishmentFormatUtils;
import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.regex.Pattern;

public class LookupShared {
    private static final Pattern namePattern = Pattern.compile("[A-z0-9]\\w+");

    protected static void printLookup(CommandSource sender, String targetPlayer, QueryMode mode, int limit, boolean debug, boolean ip) {
        try {
            String uuidOrIp;
            if (!ip) {
                uuidOrIp = MojangAccountUtils.getUUID(targetPlayer);
            } else {
                if (Utilities.validIP(targetPlayer)) {
                    uuidOrIp = targetPlayer;
                } else {
                    if (targetPlayer.length() <= 16 && namePattern.matcher(targetPlayer).matches()) {
                        uuidOrIp = Utilities.getIpAddress(targetPlayer);
                    } else throw new PlayerNotFoundException();
                }
            }

            Component textComponent = Component.empty();
            switch (mode) {
                case ALL -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_all", QVelocityPunishments.getInstance().getVersion()));

                    if (!ip) {
                        ResultSet rs = MySQLHandler.getInstance().get("SELECT * FROM `playerData` WHERE `uuid`=\"" + uuidOrIp + "\";");
                        String firstLogin = MessagesUtils.getRawString("never_logged_on");
                        String lastLogin = MessagesUtils.getRawString("never_logged_on");

                        if (rs != null && rs.next()) {
                            firstLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("firstLogin"));
                            lastLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("lastLogin"));
                        }

                        textComponent = textComponent.append(MessagesUtils.getMessage("first_login", firstLogin));
                        textComponent = textComponent.append(MessagesUtils.getMessage("last_login", lastLogin));

                    } else {

                        ResultSet playersLinked = MySQLHandler.getInstance().get("SELECT * FROM `playerData` WHERE `ip`=\"" + uuidOrIp + "\";");

                        Component ipAddress = Component.empty();
                        Component playersLinkedComponent = MessagesUtils.getMessage("lookup_ip_players_linked");
                        Component linkedPlayer;

                        if (playersLinked != null && playersLinked.next()) {
                            ipAddress = MessagesUtils.getMessage("lookup_ip_address", playersLinked.getString("ip"));
                            ipAddress = ipAddress.append(playersLinkedComponent);

                            do {
                                linkedPlayer = MessagesUtils.getMessage("lookup_ip_players_linked_format", playersLinked.getString("name"));

                                String uuid = playersLinked.getString("uuid");
                                linkedPlayer = linkedPlayer.clickEvent(ClickEvent.suggestCommand(uuid));
                                linkedPlayer = linkedPlayer.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("&7UUID: &b" + uuid)));

                                ipAddress = ipAddress.append(linkedPlayer);
                            } while (playersLinked.next());
                        }

                        textComponent = textComponent.append(ipAddress).append(Component.text("\n"));
                    }


                    ResultSet historyResults = MySQLHandler.getInstance().get("SELECT * FROM `punishments` WHERE `punished_uuid`=\"" + uuidOrIp + "\";");

                    int ban = 0;
                    int mute = 0;
                    int kick = 0;

                    if (historyResults != null) {
                        while (historyResults.next()) {
                            switch (historyResults.getString("punishment_type")) {
                                case "mute" -> mute++;
                                case "kick" -> kick++;
                                case "ban" -> ban++;
                            }
                        }
                    }

                    textComponent = textComponent.append(MessagesUtils.getMessage("history_count", ban, mute, kick));

                    textComponent = textComponent.append(Component.text("\n\n"));

                    if (!ip) {
                        textComponent = textComponent.append(CommentUtils.getCommentsOfMember(uuidOrIp, 3, false));

                        textComponent = textComponent.append(Component.text("\n"));
                    }

                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestPunishmentsOfMember(uuidOrIp, 3));

                    textComponent = textComponent.append(Component.text("\n"));

                    if (!ip) {
                        Iterator<String> namesIterator = MojangAccountUtils.getNameHistory(uuidOrIp).listIterator();

                        Component nameHistory = MessagesUtils.getMessage("name_history");

                        while (namesIterator.hasNext()) {
                            nameHistory = nameHistory.append(MessagesUtils.getMessage("name_history_format", namesIterator.next()));

                            if (namesIterator.hasNext()) {
                                nameHistory = nameHistory.append(Component.text(", "));
                            }
                        }

                        textComponent = textComponent.append(nameHistory);
                    }
                }

                case COMMENT -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Comment"));
                    textComponent = textComponent.append(CommentUtils.getCommentsOfMember(uuidOrIp, limit, debug));
                }

                case MUTE -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Mute"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.MUTE, limit));
                }

                case BAN -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Ban"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.BAN, limit));
                }

                case KICK -> {
                    textComponent = textComponent.append(MessagesUtils.getMessage("lookup_mode_specific", QVelocityPunishments.getInstance().getVersion(), "Kick"));
                    textComponent = textComponent.append(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.KICK, limit));
                }
            }

            sender.sendMessage(textComponent);

        } catch (UUIDNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        } catch (SQLException | IOException | PlayerNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
}
