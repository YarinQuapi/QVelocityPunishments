package me.yarinlevi.qpunishments.support.bungee;

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
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerChatListener;
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerConnectListener;
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerSwitchServerListener;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author YarinQuapi
 */
public final class QBungeePunishments extends Plugin {
    @Getter private static QBungeePunishments instance;
    @Getter private MySQLHandler mysql;
    @Getter private Configuration config;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists())
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdir();

        File file1 = new File(getDataFolder(), "messages.yml");
        File file2 = new File(getDataFolder(), "config.yml");

        registerFile(file1, "messages.yml");
        registerFile(file2, "config.yml");


        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            this.mysql = new MySQLHandler(this.config);

        } catch (IOException e) {
            e.printStackTrace();
        }

        new MessagesUtils();

        // Punishment commands
        this.getProxy().getPluginManager().registerCommand(this, new BanCommand("ban", "qpunishments.commands.ban", "qban", "tempban", "qtempban"));
        this.getProxy().getPluginManager().registerCommand(this, new MuteCommand("mute", "qpunishments.commands.mute", "qmute", "tempmute", "qtempmute"));
        this.getProxy().getPluginManager().registerCommand(this, new KickCommand("kick", "qpunishments.commands.kick", "qkick"));
        this.getProxy().getPluginManager().registerCommand(this, new UnBanCommand("unban", "qpunishments.commands.unban", "qunban"));
        this.getProxy().getPluginManager().registerCommand(this, new UnMuteCommand("unmute", "qpunishments.commands.unmute", "qunmute"));

        // History and proof commands
        this.getProxy().getPluginManager().registerCommand(this, new CommentCommand());
        this.getProxy().getPluginManager().registerCommand(this, new LookupCommand());
        this.getProxy().getPluginManager().registerCommand(this, new HistoryCommand());

        // Utility commands
        this.getProxy().getPluginManager().registerCommand(this, new FindCommand());
        this.getProxy().getPluginManager().registerCommand(this, new ReloadMessages());
        this.getProxy().getPluginManager().registerCommand(this, new StaffChatCommand());

        // Listeners for ban and chat control
        this.getProxy().getPluginManager().registerListener(this, new PlayerConnectListener());
        this.getProxy().getPluginManager().registerListener(this, new PlayerChatListener());
        this.getProxy().getPluginManager().registerListener(this, new PlayerSwitchServerListener());


        // BStats initialization
        Metrics metrics = new Metrics(this, 12669);
    }

    private void registerFile(File file, String streamFileName) {
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream(streamFileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
