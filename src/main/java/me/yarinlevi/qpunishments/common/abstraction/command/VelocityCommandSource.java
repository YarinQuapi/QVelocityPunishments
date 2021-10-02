package me.yarinlevi.qpunishments.common.abstraction.command;

import com.velocitypowered.api.permission.PermissionSubject;
import net.kyori.adventure.audience.Audience;

public interface VelocityCommandSource extends Audience, PermissionSubject {
}
