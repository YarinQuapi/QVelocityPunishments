package me.yarinlevi.qpunishments.support.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class PlayerChatListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) throws SQLException {
        Player sender = event.getPlayer();

        ResultSet rs = QVelocityPunishments.getInstance().getMysql().get(String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"mute\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"mute\"",
                sender.getUniqueId(), System.currentTimeMillis(), sender.getUniqueId()));

        if (rs != null && rs.next()) {
            String server = rs.getString("server");

            if (server.equalsIgnoreCase("global") || sender.getCurrentServer().get().getServerInfo().getName().equals(server)) {
                event.setResult(PlayerChatEvent.ChatResult.denied());

                long timestamp = rs.getLong("expire_date");

                String formattedDate;
                if (timestamp != 0) {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                } else {
                    formattedDate = "forever";
                }

                sender.sendMessage(MessagesUtils.getMessage("you_are_muted", formattedDate));
            }
        }

        if (sender.hasPermission("qpunishments.commands.staffchat")) {
            if (event.getMessage().startsWith(QVelocityPunishments.getInstance().getConfig().getString("staff-chat.chat-char"))) {
                event.setResult(PlayerChatEvent.ChatResult.denied());

                StringBuffer sb = new StringBuffer(event.getMessage());

                sb.deleteCharAt(0);

                for (Player proxiedPlayer : QVelocityPunishments.getInstance().getServer().getAllPlayers().stream().filter(x ->x .hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                    proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", sender.getUsername(), sb.toString()));
                }
            }
        }
    }
}