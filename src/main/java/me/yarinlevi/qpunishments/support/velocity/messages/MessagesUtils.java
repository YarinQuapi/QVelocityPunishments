package me.yarinlevi.qpunishments.support.velocity.messages;

import me.yarinlevi.qpunishments.support.velocity.QVelocityPunishments;
import me.yarinlevi.qpunishments.utilities.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessagesUtils {
    private static final Map<String, String> messages = new HashMap<>();
    static Pattern urlPattern = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

    public MessagesUtils() {
        Configuration messagesData;

        messagesData = new Configuration(QVelocityPunishments.getInstance().getPath().toString() + "/messages.yml");
        messagesData.getKeys().forEach(key -> messages.put(key, messagesData.getString(key)));
    }

    public static void reload() {
        messages.clear();

        Configuration messagesData;

        messagesData = new Configuration(QVelocityPunishments.getInstance().getPath().toString() + "/messages.yml");
        messagesData.getKeys().forEach(key -> messages.put(key, messagesData.getString(key)));
    }

    public static Component getMessage(String key, Object... args) {
        return Component.text(String.format(messages.get(key).replaceAll("&", "§"), args));
    }

    /*
    public static Component getMessageWithClickable(String key, Object... args) {
        String msg = String.format(messages.get(key).replaceAll("&", "§"), args);

        Component textComponent = Component.empty();

        for (String str : msg.split(" ")) {
            if (urlPattern.matcher(str).matches()) {
                Component uri = Component.text(str);

                uri = uri.clickEvent(ClickEvent.openUrl(str));
                textComponent = textComponent.append(uri).append(Component.text(" "));
            } else {
                Component noteArg = Component.text(str);

                textComponent = textComponent.append(noteArg).append(Component.text(" "));
            }
        }

        return textComponent;
    }
     */

    public static Component getMessageWithClickable(String msg, Object... args) {
        Component text = Component.text(messages.get(msg).replaceAll("&", "§").formatted(args));

        text = text.replaceText(TextReplacementConfig.builder().match(urlPattern)
                .replacement((matchResult, builder) -> {
            Component comp = Component.text(matchResult.group());
            return comp.clickEvent(ClickEvent.openUrl(matchResult.group()));
        }).build());

        return text;
    }

    public static String getRawFormattedString(String key, Object... args) {
        return messages.getOrDefault(key, key).replaceAll("&", "§").formatted(args);
    }

    public static String getRawString(String key) {
        return messages.getOrDefault(key, key).replaceAll("&", "§");
    }
}
