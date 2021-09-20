package me.yarinlevi.qpunishments.utilities;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import net.kyori.adventure.text.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class Utilities {
    private static final Pattern ipPattern = Pattern
            .compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    public static void broadcast(Component message) {
        for (Player player : QVelocityPunishments.getInstance().getServer().getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    public static boolean validIP(final String ip) {
        return ipPattern.matcher(ip).matches();
    }

    public static String getIpAddress(String playerName) throws PlayerNotFoundException {
        if (QVelocityPunishments.getInstance().getServer().getPlayer(playerName).isPresent())
            return QVelocityPunishments.getInstance().getServer().getPlayer(playerName).get().getRemoteAddress().getHostName();

        ResultSet rs = QVelocityPunishments.getInstance().getMysql().get(String.format("SELECT * FROM `playerData` WHERE `name`=\"%s\" ORDER BY lastLogin DESC;",
                playerName));

        try {
            if (rs != null && rs.next()) {
                return rs.getString("ip");
            } else {
                throw new PlayerNotFoundException();
            }
        } catch (SQLException e) {
            throw new PlayerNotFoundException();
        }
    }
}
