package me.yarinlevi.qpunishments.utilities;

import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import net.kyori.adventure.text.Component;

public class Utilities {
    public static void broadcast(Component message) {
        for (Player player : QVelocityPunishments.getInstance().getServer().getAllPlayers()) {
            player.sendMessage(message);
        }
    }
}
