package me.yarinlevi.qpunishments.common.abstraction.player;

public abstract class QPlayer {
    public abstract void sendMessage(String key, Object... args);

    public abstract void sendMessageLink(String key, Object... args);
}
