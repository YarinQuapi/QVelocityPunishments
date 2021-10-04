package me.yarinlevi.qpunishments.support.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishmentsBoot;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

public class PlayerSwitchServerListener {
    @Subscribe
    public void onSwitchServer(ServerPreConnectEvent event) throws SQLException {
        if (event.getPlayer().isActive()) {
            String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"ban\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"ban\"",
                    event.getPlayer().getUniqueId().toString(), System.currentTimeMillis(), event.getPlayer().getUniqueId().toString());

            ResultSet rs = QVelocityPunishmentsBoot.getInstance().getMysql().get(sql);

            try {
                if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date") && rs.getString("server").equals(event.getPlayer().getCurrentServer().orElseThrow().getServerInfo().getName())) {
                    long timestamp = rs.getLong("expire_date");

                    String formattedDate;
                    if (timestamp != 0) {
                        formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                    } else formattedDate = "Never";

                    //String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format((rs.getTimestamp("timestamp")));
                    String reason = rs.getString("reason");

                    event.getPlayer().sendMessage(MessagesUtils.getMessage("you_are_banned_chat", reason, formattedDate));
                }
            } catch (NoSuchElementException ignored) { }
        }
    }
}
