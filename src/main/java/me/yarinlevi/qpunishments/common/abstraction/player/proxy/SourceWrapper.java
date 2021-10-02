package me.yarinlevi.qpunishments.common.abstraction.player.proxy;

import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

public abstract class SourceWrapper<T> implements AutoCloseable {
    protected abstract UUID getUniqueId(T sender);

    protected abstract String getName(T sender);

    protected abstract void sendMessage(T sender, Component message);

    protected abstract boolean hasPermission(T sender, String node);

    protected abstract void performCommand(T sender, String command);

    protected abstract boolean isConsole(T sender);

    public final ICommandSender wrap(T sender) {
        Objects.requireNonNull(sender, "sender");
        return new AbstractSender<>(this, sender);
    }

    @Override
    public void close() {

    }
}
