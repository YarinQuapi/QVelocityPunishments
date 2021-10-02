package me.yarinlevi.qpunishments.support.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import me.yarinlevi.qpunishments.common.abstraction.player.proxy.ICommandSender;
import me.yarinlevi.qpunishments.common.abstraction.player.proxy.SourceWrapper;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocitySourceWrapper extends SourceWrapper<CommandSource> {
    @Override
    protected UUID getUniqueId(CommandSource sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        }
        return ICommandSender.CONSOLE_UUID;
    }

    @Override
    protected String getName(CommandSource sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUsername();
        }
        return ICommandSender.CONSOLE_NAME;
    }

    @Override
    protected void sendMessage(CommandSource sender, Component message) {
        sender.sendMessage(message);
    }

    @Override
    protected boolean hasPermission(CommandSource sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected void performCommand(CommandSource sender, String command) {
        QVelocityPunishments.getInstance().getServer().getCommandManager().executeAsync(sender, command);
    }

    @Override
    protected boolean isConsole(CommandSource sender) {
        return sender instanceof ConsoleCommandSource;
    }
}
