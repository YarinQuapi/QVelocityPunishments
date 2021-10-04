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
import me.yarinlevi.qpunishments.support.velocity.commands.administration.ReloadMessagesCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.executing.*;
import me.yarinlevi.qpunishments.support.velocity.commands.removing.UnBanCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.removing.UnIpBanCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.removing.UnIpMuteCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.removing.UnMuteCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.utilities.CommentCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.utilities.HistoryCommand;
import me.yarinlevi.qpunishments.support.velocity.commands.utilities.LookupCommand;
import me.yarinlevi.qpunishments.support.velocity.general.VelocitySourceWrapper;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerChatListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerConnectListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerSwitchServerListener;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.Configuration;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import org.bstats.velocity.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author YarinQuapi
 */
@Plugin(id = "qvelocitypunishments", name = "QVelocityPunishments", version = "0.1.2A-PrivateVelocity",
        description = "An all-in-one punishment system for Minecraft proxies", authors = {"Quapi"})
public final class QVelocityPunishmentsBoot {
    @Getter private final ProxyServer server;
    @Getter private final Logger logger;
    private final Metrics.Factory metricsFactory;
    @Getter private final Path path;

    @Getter private final String version = "0.1.2A-PrivateVelocity";
    @Getter private static QVelocityPunishmentsBoot instance;
    @Getter private final QVPunishments<QVelocityPunishmentsBoot> plugin;

    @Inject
    public QVelocityPunishmentsBoot(ProxyServer server, Logger logger, @DataDirectory Path directory, Metrics.Factory metricsFactory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.path = directory;
        this.metricsFactory = metricsFactory;
        this.plugin = new QVPunishments<>(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin.enable(path.toFile());

        new MessagesUtils();

        CommandManager commandManager = server.getCommandManager();
        EventManager eventManager = server.getEventManager();

        // Punishment commands
        commandManager.register("ban", new BanCommand(), "qban", "tempban", "qtempban");
        commandManager.register("mute", new MuteCommand(), "qmute", "tempmute", "qtempmute");
        commandManager.register("kick", new KickCommand(), "qkick");
        commandManager.register("unban", new UnBanCommand(), "qunban");
        commandManager.register("unmute", new UnMuteCommand(), "qunmute");

        // Ip Punishment commands
        commandManager.register("ipban", new IpBanCommand(), "qipban", "qiptempban");
        commandManager.register("ipmute", new IpMuteCommand(), "qipmute", "qiptempmute");
        commandManager.register("unipban", new UnIpBanCommand(), "qunipban");
        commandManager.register("unipmute", new UnIpMuteCommand(), "qunipmute");

        // History and proof commands
        commandManager.register("comment", new CommentCommand(), "addcomment");
        commandManager.register("lookup", new LookupCommand());
        commandManager.register("history", new HistoryCommand(), "ha", "historyadmin");

        // Utilities
        commandManager.register("reloadmessages", new ReloadMessagesCommand());


        // Listeners for ban and chat control
        PlayerChatListener chatListener = new PlayerChatListener();

        eventManager.register(this, new PlayerConnectListener());
        eventManager.register(this, chatListener);
        eventManager.register(this, new PlayerSwitchServerListener());

        if (this.getServer().getPluginManager().isLoaded("qproxyutilities-velocity")) {
            chatListener.setQProxyUtilitiesFound(true);
            QVelocityPunishmentsBoot.getInstance().getLogger().log(Level.WARNING, "QProxyUtilities found! staff chat disabled.");
        }

        // BStats initialization
        Metrics metrics = metricsFactory.make(this, 12866);
    }
}
