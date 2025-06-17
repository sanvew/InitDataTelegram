package io.github.sanvew.tg.init.data.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Telegram User object as received from the init data payload.
 * <p>
 * Supports known fields such as {@code added_to_attachment_menu}, {@code allows_write_to_pm}, {@code is_premium},
 * {@code first_name}, {@code id}, {@code is_bot}, {@code last_name}, {@code language_code}, {@code photo_url} and {@code username},
 * and allows storing unknown extra fields as a map via {@link #getExtra()}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#user">Telegram Mini Apps Init Data: Chat</a>
 */public class User {
    public enum Property {
        ADDED_TO_ATTACHMENT_MENU("added_to_attachment_menu"),
        ALLOWS_WRITE_TO_PM ("allows_write_to_pm"),
        IS_PREMIUM("is_premium"),
        FIRST_NAME("first_name"),
        ID("id"),
        IS_BOT("is_bot"),
        LAST_NAME("last_name"),
        LANGUAGE_CODE("language_code"),
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

    private final Boolean addedToAttachmentMenu;
    private final Boolean allowsWriteToPm;
    private final Boolean isPremium;
    private final String firstName;
    private final long id;
    private final Boolean isBot;
    private final String lastName;
    private final String languageCode;
    private final String photoUrl;
    private final String username;
    private final Map<String, String> extra;

    public User(
            @Nullable Boolean addedToAttachmentMenu,
            @Nullable Boolean allowsWriteToPm,
            @Nullable Boolean isPremium,
            @NotNull String firstName,
            long id,
            @Nullable Boolean isBot,
            @Nullable String lastName,
            @Nullable String languageCode,
            @Nullable String photoUrl,
            @Nullable String username,
            @Nullable Map<String, String> extra
    ) {
        this.addedToAttachmentMenu = addedToAttachmentMenu;
        this.allowsWriteToPm = allowsWriteToPm;
        this.isPremium = isPremium;
        this.firstName = firstName;
        this.id = id;
        this.isBot = isBot;
        this.lastName = lastName;
        this.languageCode = languageCode;
        this.photoUrl = photoUrl;
        this.username = username;
        this.extra = extra == null ? Map.of() : extra;
    }

    public User(
            @Nullable Boolean addedToAttachmentMenu,
            @Nullable Boolean allowsWriteToPm,
            @Nullable Boolean isPremium,
            @NotNull String firstName,
            long id,
            @Nullable Boolean isBot,
            @Nullable String lastName,
            @Nullable String languageCode,
            @Nullable String photoUrl,
            @Nullable String username
    ) {
        this(
                addedToAttachmentMenu, allowsWriteToPm, isPremium, firstName, id, isBot, lastName, languageCode,
                photoUrl, username, null
        );
    }

    public @Nullable Boolean isAddedToAttachmentMenu() { return addedToAttachmentMenu; }
    public @Nullable Boolean allowsWriteToPm() { return allowsWriteToPm; }
    public @Nullable Boolean isPremium() { return isPremium; }
    public @NotNull String getFirstName() { return firstName; }
    public long getId() { return id; }
    public @Nullable Boolean isBot() { return isBot; }
    public @Nullable String getLastName() { return lastName; }
    public @Nullable String getLanguageCode() { return languageCode; }
    public @Nullable String getPhotoUrl() { return photoUrl; }
    public @Nullable String getUsername() { return username; }
    public @NotNull Map<String, String> getExtra() { return extra; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id
                && Objects.equals(addedToAttachmentMenu, user.addedToAttachmentMenu)
                && Objects.equals(allowsWriteToPm, user.allowsWriteToPm)
                && Objects.equals(isPremium, user.isPremium)
                && Objects.equals(firstName, user.firstName)
                && Objects.equals(isBot, user.isBot)
                && Objects.equals(lastName, user.lastName)
                && Objects.equals(languageCode, user.languageCode)
                && Objects.equals(photoUrl, user.photoUrl)
                && Objects.equals(username, user.username)
                && Objects.equals(extra, user.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                addedToAttachmentMenu, allowsWriteToPm, isPremium, firstName, id, isBot, lastName, languageCode,
                photoUrl, username, extra
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "addedToAttachmentMenu=" + addedToAttachmentMenu +
                ", allowsWriteToPm=" + allowsWriteToPm +
                ", isPremium=" + isPremium +
                ", firstName='" + firstName + '\'' +
                ", id=" + id +
                ", isBot=" + isBot +
                ", lastName='" + lastName + '\'' +
                ", languageCode='" + languageCode + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", username='" + username + '\'' +
                ", extra=" + extra +
                '}';
    }
}