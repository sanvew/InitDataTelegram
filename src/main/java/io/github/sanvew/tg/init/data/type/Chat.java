package io.github.sanvew.tg.init.data.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Telegram Chat object as received from the init data payload.
 * <p>
 * Supports known fields such as {@code id}, {@code type}, {@code title}, {@code photo_url} and {@code username},
 * and allows storing unknown extra fields as a map via {@link #getExtra()}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#chat">Telegram Mini Apps Init Data: Chat</a>
 */
public class Chat {
    public enum Property {
        ID("id"),
        TYPE("type"),
        TITLE("title"),
        PHOTO_URL("photo_url"),
        USERNAME("username"),
        ;

        private static final Set<String> KNOWN = Arrays.stream(Property.values())
                .map(it -> it.value)
                .collect(Collectors.toSet());

        public final String value;

        Property(String value) {
            this.value = value;
        }

        public static boolean isNotKnown(String property) {
            return !KNOWN.contains(property);
        }
    }

    final private long id;
    final private ChatType type;
    final private String title;
    final private String photoUrl;
    final private String username;
    private final Map<String, String> extra;

    public Chat(
            long id,
            @NotNull ChatType type,
            @NotNull String title,
            @Nullable String photoUrl,
            @Nullable String username,
            @Nullable Map<String, String> extra
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.photoUrl = photoUrl;
        this.username = username;
        this.extra = extra == null ? Map.of() : extra;
    }

    public Chat(
            long id,
            @NotNull ChatType type,
            @NotNull String title,
            @Nullable String photoUrl,
            @Nullable String username
    ) {
        this(id, type, title, photoUrl, username, null);
    }

    public long getId() {return id; }
    public @NotNull ChatType getType() {return type; }
    public @NotNull String getTitle() { return title; }
    public @Nullable String getPhotoUrl() { return photoUrl; }
    public @Nullable String getUsername() { return username; }
    public @NotNull Map<String, String> getExtra() { return extra; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        Chat chat = (Chat) o;
        return id == chat.id
                && type == chat.type
                && Objects.equals(title, chat.title)
                && Objects.equals(photoUrl, chat.photoUrl)
                && Objects.equals(username, chat.username)
                && Objects.equals(extra, chat.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, photoUrl, username, extra);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", username='" + username + '\'' +
                ", extra=" + extra +
                '}';
    }
}
