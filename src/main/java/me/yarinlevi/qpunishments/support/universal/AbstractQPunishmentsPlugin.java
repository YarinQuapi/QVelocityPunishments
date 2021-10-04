package me.yarinlevi.qpunishments.support.universal;

import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qpunishments.support.universal.commands.SourceWrapper;
import me.yarinlevi.qpunishments.utilities.Configuration;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public abstract class AbstractQPunishmentsPlugin<P> implements QPunishmentsPlugin<P> {
    @Getter private MySQLHandler mySQLHandler;
    @Setter private SourceWrapper<?> sourceWrapper;
    @Getter private Configuration pluginConfig;

    public void enable(File file) {
        if (!file.exists())
            //noinspection ResultOfMethodCallIgnored
            file.mkdir();

        File file1 = new File(file, "messages.yml");
        File file2 = new File(file, "config.yml");

        registerFile(file1, "messages.yml");
        registerFile(file2, "config.yml");

        this.pluginConfig = new Configuration(file + "\\config.yml");

        setupSourceWrapper();

        mySQLHandler = new MySQLHandler(this.pluginConfig);
    }

    public abstract void setupSourceWrapper();

    public SourceWrapper<?> getSourceWrapper() {
        return this.sourceWrapper;
    }

    public abstract boolean isServer(String serverName);

    private void registerFile(File file, String streamFileName) {
        if (!file.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(streamFileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
