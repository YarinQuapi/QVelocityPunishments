package me.yarinlevi.qpunishments.support.velocity;

import me.yarinlevi.qpunishments.support.universal.AbstractQPunishmentsPlugin;
import me.yarinlevi.qpunishments.support.velocity.general.VelocitySourceWrapper;

public class QVPunishments<P> extends AbstractQPunishmentsPlugin<P> {
    QVelocityPunishmentsBoot velocityPunishments;

    public QVPunishments(QVelocityPunishmentsBoot plugin) {
        velocityPunishments = plugin;
    }

    @Override
    public void setupSourceWrapper() {
        this.setSourceWrapper(new VelocitySourceWrapper());
    }

    @Override
    public Object getPlugin() {
        return velocityPunishments;
    }
}
