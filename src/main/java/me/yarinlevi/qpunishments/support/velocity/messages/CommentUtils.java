package me.yarinlevi.qpunishments.support.velocity.messages;

import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishmentsBoot;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import net.kyori.adventure.text.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class CommentUtils {
    public static Component getCommentsOfMember(String uuid, int count, boolean debug) throws PlayerNotFoundException, SQLException {
        ResultSet resultSet = QVelocityPunishmentsBoot.getInstance().getMysql().get(String.format("SELECT * FROM `comments` WHERE `punished_uuid`=\"%s\" ORDER BY date_added DESC LIMIT " + count, uuid));

        String name = MojangAccountUtils.getName(uuid);

        if (resultSet != null && resultSet.next()) {
            Component stringBuilder = MessagesUtils.getMessage("last_comments", count, name);

            do {
                Component comment;
                if (!debug) {
                    comment = MessagesUtils.getMessageWithClickable("comment_format",
                            resultSet.getString("content"),
                            MojangAccountUtils.getName(resultSet.getString("punished_by_uuid")),
                            resultSet.getString("punished_by_name"),
                            new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(resultSet.getLong("date_added")));
                } else {
                    comment = MessagesUtils.getMessageWithClickable("comment_format_debug",
                            resultSet.getString("content"),
                            MojangAccountUtils.getName(resultSet.getString("punished_by_uuid")),
                            resultSet.getString("punished_by_name"),
                            new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(resultSet.getLong("date_added")),
                            resultSet.getInt("id"));
                }
                stringBuilder = stringBuilder.append(comment);
            } while (resultSet.next());

            return stringBuilder;
        } else {
            return MessagesUtils.getMessage("no_comments");
        }
    }
}
