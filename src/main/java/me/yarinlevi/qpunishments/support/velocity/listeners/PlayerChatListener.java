package me.yarinlevi.qpunishments.support.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Setter;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.RedisHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class PlayerChatListener {
    private final boolean staffChatEnabled = QVelocityPunishments.getInstance().getConfig().getBoolean("staff-chat.enabled");
    @Setter private boolean isQProxyUtilitiesFound = false;

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) throws SQLException {
        Player sender = event.getPlayer();

        String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"mute\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"mute\" ORDER BY id DESC;",
                event.getPlayer().getUniqueId().toString(), System.currentTimeMillis(), event.getPlayer().getUniqueId().toString());

        ResultSet rs = QVelocityPunishments.getInstance().getMysql().get(sql);

        if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date")) {
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
                return;
            }
        }

        if (sender.hasPermission("qpunishments.commands.staffchat") && staffChatEnabled) {
            if (event.getMessage().startsWith(QVelocityPunishments.getInstance().getConfig().getString("staff-chat.chat-char"))) {
                if (!isQProxyUtilitiesFound) {
                    event.setResult(PlayerChatEvent.ChatResult.denied());

                    StringBuffer sb = new StringBuffer(event.getMessage());

                    sb.deleteCharAt(0);

                    if (!RedisHandler.isRedis()) {
                        for (Player proxiedPlayer : QVelocityPunishments.getInstance().getServer().getAllPlayers().stream().filter(x -> x.hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                            proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", sender.getUsername(), sb.toString()));
                        }
                    } else {
                        QVelocityPunishments.getInstance().getRedis().postStaffChatMessage(MessagesUtils.getRawFormattedString("staff_chat_message", sender.getUsername(), sb.toString()));
                    }
                }
            }
        }
    }
}