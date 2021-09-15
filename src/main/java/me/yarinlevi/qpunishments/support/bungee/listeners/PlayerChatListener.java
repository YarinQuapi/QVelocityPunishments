package me.yarinlevi.qpunishments.support.bungee.listeners;

import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(ChatEvent event) throws SQLException {
        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();

        ResultSet rs = QBungeePunishments.getInstance().getMysql().get(String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"mute\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"mute\"",
                sender.getUniqueId(), System.currentTimeMillis(), sender.getUniqueId()));

        if (rs != null && rs.next()) {
            String server = rs.getString("server");

            if (server.equalsIgnoreCase("global") || sender.getServer().getInfo().getName().equals(server)) {
                event.setCancelled(true);

                long timestamp = rs.getLong("expire_date");

                String formattedDate;
                if (timestamp != 0) {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                } else {
                    formattedDate = "forever";
                }

                if (event.isCommand() || event.isProxyCommand()) {
                    if (QBungeePunishments.getInstance().getConfig().getStringList("blocked-commands").contains(event.getMessage().split(" ")[0].toLowerCase())) {
                        event.setCancelled(true); // Todo: check if #setCanceled cancels commands
                    }
                }

                sender.sendMessage(MessagesUtils.getMessage("you_are_muted", formattedDate));
            }
        }

        if (sender.hasPermission("qpunishments.commands.staffchat")) {
            if (event.getMessage().startsWith(QBungeePunishments.getInstance().getConfig().getString("staff-chat.chat-char"))) {
                event.setCancelled(true);

                StringBuffer sb = new StringBuffer(event.getMessage());

                sb.deleteCharAt(0);

                for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers().stream().filter(x ->x .hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                    proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", sender.getName(), sb.toString()));
                }
            }
        }
    }
}