package me.yarinlevi.qpunishments.common.abstraction;

import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.common.abstraction.player.QPlayer;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;

public abstract class VelocityPlayer extends QPlayer implements Player {
    @Override
    public void sendMessage(String key, Object... args) {
        this.sendMessage(MessagesUtils.getMessage(key, args));
    }

    @Override
    public void sendMessageLink(String key, Object... args) {
        this.sendMessage(MessagesUtils.getMessageWithClickable(key, args));
    }
}
