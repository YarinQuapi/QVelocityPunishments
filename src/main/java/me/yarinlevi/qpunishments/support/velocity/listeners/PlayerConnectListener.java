package me.yarinlevi.qpunishments.support.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * @author YarinQuapi
 */
public class PlayerConnectListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onConnect(PostLoginEvent event) throws SQLException {
            String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"ban\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"ban\" ORDER BY id DESC;",
                    event.getPlayer().getUniqueId().toString(), System.currentTimeMillis(), event.getPlayer().getUniqueId().toString());

            ResultSet rs = QVelocityPunishments.getInstance().getMysql().get(sql);

            if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date") && rs.getString("server").equalsIgnoreCase("global")) {
                long timestamp = rs.getLong("expire_date");

                String formattedDate;
                if (timestamp != 0) {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                } else formattedDate = "Never";

                //String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format((rs.getTimestamp("timestamp")));
                String reason = rs.getString("reason");

                event.getPlayer().disconnect(MessagesUtils.getMessage("you_are_banned_disconnect", reason, formattedDate));
                return;
            }

            // PlayerData Construction

            String sql1 = String.format("SELECT * FROM playerData WHERE `uuid`=\"%s\";", event.getPlayer().getUniqueId());

            ResultSet rs1 = QVelocityPunishments.getInstance().getMysql().get(sql1);

            if (rs1 != null && rs1.next()) {
                String sql2 = String.format("UPDATE `playerData` set `lastLogin`=\"%s\", `name`=\"%s\" WHERE `uuid`=\"%s\";",
                        System.currentTimeMillis(),
                        event.getPlayer().getUsername(),
                        event.getPlayer().getUniqueId());

                QVelocityPunishments.getInstance().getServer().getScheduler().buildTask(QVelocityPunishments.getInstance(), () -> QVelocityPunishments.getInstance().getMysql().update(sql2)).schedule();
            } else {
                String sql2 = String.format("INSERT INTO `playerData`(`uuid`, `name`, `firstLogin`, `lastLogin`) VALUES(\"%s\", \"%s\",\"%s\", \"%s\");",
                        event.getPlayer().getUniqueId(),
                        event.getPlayer().getUsername(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis());

                QVelocityPunishments.getInstance().getServer().getScheduler().buildTask(QVelocityPunishments.getInstance(), () -> QVelocityPunishments.getInstance().getMysql().update(sql2)).schedule();
            }
    }
}
