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
import lombok.Setter;
import me.yarinlevi.qpunishments.commands.CommentCommand;
import me.yarinlevi.qpunishments.commands.LookupCommand;
import me.yarinlevi.qpunishments.commands.LookupIpCommand;
import me.yarinlevi.qpunishments.commands.administration.ReloadMessagesCommand;
import me.yarinlevi.qpunishments.commands.executing.*;
import me.yarinlevi.qpunishments.commands.removing.UnBanCommand;
import me.yarinlevi.qpunishments.commands.removing.UnIpBanCommand;
import me.yarinlevi.qpunishments.commands.removing.UnIpMuteCommand;
import me.yarinlevi.qpunishments.commands.removing.UnMuteCommand;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerChatListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerConnectListener;
import me.yarinlevi.qpunishments.support.velocity.listeners.PlayerSwitchServerListener;
import me.yarinlevi.qpunishments.support.velocity.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.Configuration;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import me.yarinlevi.qpunishments.utilities.RedisHandler;
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
@Plugin(id = "qvelocitypunishments", name = "QVelocityPunishments", version = "0.1.4B-PublicVelocity",
        description = "An all-in-one punishment system for Minecraft proxies", authors = {"Quapi"})
public final class QVelocityPunishments {
    @Getter private final ProxyServer server;
    @Getter private final Logger logger;
    private final Metrics.Factory metricsFactory;
    @Getter private final Path path;

    @Getter private final String version = "0.1.4B-PublicVelocity";
    @Getter private static QVelocityPunishments instance;
    @Getter private MySQLHandler mysql;
    @Getter private RedisHandler redis;
    @Getter private Configuration config;

    @Getter @Setter private boolean lockdownMode = false;

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
        if (getJavaVersion() < 16) {
            this.getLogger().severe("QVelocityPunishments requires Java 16+ to operate. Please upgrade your software and try again.");
            return;
        }

        if (!path.toFile().exists())
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdir();

        File file1 = new File(path.toFile(), "messages.yml");
        File file2 = new File(path.toFile(), "config.yml");

        registerFile(file1, "messages.yml");
        registerFile(file2, "config.yml");


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

        // Ip Punishment commands
        commandManager.register("ipban", new IpBanCommand(), "qipban", "qiptempban");
        commandManager.register("ipmute", new IpMuteCommand(), "qipmute", "qiptempmute");
        commandManager.register("unipban", new UnIpBanCommand(), "qunipban");
        commandManager.register("unipmute", new UnIpMuteCommand(), "qunipmute");

        // History and proof commands
        commandManager.register("comment", new CommentCommand(), "addcomment");
        commandManager.register("lookup", new LookupCommand());
        commandManager.register("lookupip", new LookupIpCommand());
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
            QVelocityPunishments.getInstance().getLogger().log(Level.WARNING, "QProxyUtilities found! staff chat disabled.");
        }

        if (this.isRedis()) {
            redis = new RedisHandler(this.config);
        }

        // BStats initialization
        metricsFactory.make(this, 12866);
    }

    private void registerFile(File file, String streamFileName) {
        if (!file.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(streamFileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    public boolean isRedis() {
        return this.config.getBoolean("redis.enabled");
    }
}
