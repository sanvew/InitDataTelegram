package io.github.sanvew.tg.init.data.type;

import org.jetbrains.annotations.Nullable;

/**
 * Enumeration of possible chat types in Telegram Mini App Init Data.
 * <p>
 * Supported types include {@code sender}, {@code private}, {@code group}, {@code supergroup}, and {@code channel}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#chat">Telegram Init Data - chat.type</a>
 */
public enum ChatType {
    SENDER("sender"),
    PRIVATE("private"),
    GROUP("group"),
    SUPERGROUP("supergroup"),
    CHANNEL("channel"),
    ;

    public final String value;

    ChatType(String value) {
        this.value = value;
    }

    /**
     * Parses a string value into a corresponding {@link ChatType} enum.
     *
     * @param value the string representation of the chat type (e.g. "group")
     * @return the matching {@link ChatType} or {@code null} if input is null
     * @throws IllegalArgumentException if the value is not recognized
     */
    public static @Nullable ChatType fromValue(@Nullable String value) {
        if (value == null) { return null; }
        for (final ChatType chatType : ChatType.values()) {
            if (chatType.value.equals(value)) {
                return chatType;
            }
        }
        throw new IllegalArgumentException("Unknown value \"" + value + "\"");
    }
}
