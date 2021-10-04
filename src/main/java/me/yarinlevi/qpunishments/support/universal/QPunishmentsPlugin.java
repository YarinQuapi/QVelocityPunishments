package me.yarinlevi.qpunishments.support.universal;

import me.yarinlevi.qpunishments.support.universal.commands.SourceWrapper;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;

public interface QPunishmentsPlugin<P> {
    SourceWrapper<?> getSourceWrapper();

    MySQLHandler getMySQLHandler();

    Object getPlugin();
}
