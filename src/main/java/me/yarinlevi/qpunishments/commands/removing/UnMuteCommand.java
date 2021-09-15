package me.yarinlevi.qpunishments.commands.removing;

import me.yarinlevi.qpunishments.punishments.PunishmentType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class UnMuteCommand extends Command {
    public UnMuteCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandUtils.remove(sender, args, PunishmentType.MUTE);
    }
}
