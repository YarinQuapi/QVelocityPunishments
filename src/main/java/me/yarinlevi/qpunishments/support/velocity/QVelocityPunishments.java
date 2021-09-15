package me.yarinlevi.qpunishments.support.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.yarinlevi.qpunishments.commands.CommentCommand;
import me.yarinlevi.qpunishments.commands.LookupCommand;
import me.yarinlevi.qpunishments.commands.executing.BanCommand;
import me.yarinlevi.qpunishments.commands.executing.HistoryCommand;
import me.yarinlevi.qpunishments.commands.executing.KickCommand;
import me.yarinlevi.qpunishments.commands.executing.MuteCommand;
import me.yarinlevi.qpunishments.commands.removing.UnBanCommand;
import me.yarinlevi.qpunishments.commands.removing.UnMuteCommand;
import me.yarinlevi.qpunishments.commands.utilities.FindCommand;
import me.yarinlevi.qpunishments.commands.utilities.ReloadMessages;
import me.yarinlevi.qpunishments.commands.utilities.StaffChatCommand;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerChatListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerConnectListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerSwitchServerListener;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.Configuration;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import org.bstats.velocity.Metrics;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * @author YarinQuapi
 */
@Plugin(id = "qvelocitypunishments", name = "QVelocityPunishments", version = "0.1-Velocity",
        description = "An all-in-one punishment system for Minecraft proxies", authors = {"Quapi"})
public final class QVelocityPunishments {
    @Getter private final ProxyServer server;
    @Getter private final Logger logger;
    private final Metrics.Factory metricsFactory;
    @Getter private final Path path;

    @Getter private final String version = "0.1-Velocity";
    @Getter private static QVelocityPunishments instance;
    @Getter private MySQLHandler mysql;
    @Getter private Configuration config;

    @Inject
    public QVelocityPunishments(ProxyServer server, Logger logger, @DataDirectory Path directory, Metrics.Factory metricsFactory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.path = directory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!path.toFile().exists())
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdir();


        this.config = new Configuration(path.toFile() + "/config.yml");
        this.mysql = new MySQLHandler(this.config);

        new MessagesUtils();

        CommandManager commandManager = server.getCommandManager();
        EventManager eventManager = server.getEventManager();

        // Punishment commands
        commandManager.register("ban", new BanCommand(), "qban", "tempban", "qtempban");
        commandManager.register("mute", new MuteCommand(), "qmute", "tempmute", "qtempmute");
        commandManager.register("kick", new KickCommand(), "qkick");
        commandManager.register("unban", new UnBanCommand(), "qunban");
        commandManager.register("unmute", new UnMuteCommand(), "qunmute");

        // History and proof commands
        commandManager.register("comment", new CommentCommand(), "addcomment");
        commandManager.register("lookup", new LookupCommand());
        commandManager.register("history", new HistoryCommand(), "ha", "historyadmin");

        // Utility commands
        commandManager.register("find", new FindCommand(), "locate");
        commandManager.register("reloadmessages", new ReloadMessages());
        commandManager.register("staffchat", new StaffChatCommand(), "sc");

        // Listeners for ban and chat control
        eventManager.register(this, new PlayerConnectListener());
        eventManager.register(this, new PlayerChatListener());
        eventManager.register(this, new PlayerSwitchServerListener());


        // BStats initialization
        Metrics metrics = metricsFactory.make(this, 12669);
    }
}