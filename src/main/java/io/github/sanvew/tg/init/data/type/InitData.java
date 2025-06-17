package io.github.sanvew.tg.init.data.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the full set of Telegram Init Data parameters passed to a Web App.
 * <p>
 * This class models all known fields provided by Telegram, including both required and optional values.
 * It is intended to be used for parsing, validating, and working with authenticated init data payloads.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#parameters-list">Telegram Init Data – Parameters List</a>
 */public class InitData {
    public enum Param {
        AUTH_DATE("auth_date"),
        CAN_SEND_AFTER("can_send_after"),
        CHAT("chat"),
        CHAT_TYPE("chat_type"),
        CHAT_INSTANCE("chat_instance"),
        HASH("hash"),
        QUERY_ID("query_id"),
        RECEIVER("receiver"),
        START_PARAM("start_param"),
        USER("user"),
        ;
        public final String value;

        Param(String value) {
            this.value = value;
        }
    }

    private final long authDate;
    private final Long canSendAfter;
    private final Chat chat;
    private final ChatType chatType;
    private final String chatInstance;
    private final String hash;
    private final String queryId;
    private final User receiver;
    private final String startParam;
    private final User user;
    private final Map<String, String> extra;

    public InitData(
            long authDate,
            @Nullable Long canSendAfter,
            @Nullable Chat chat,
            @Nullable ChatType chatType,
            @Nullable String chatInstance,
            @NotNull String hash,
            @Nullable String queryId,
            @Nullable User receiver,
            @Nullable String startParam,
            @Nullable User user,
            @Nullable Map<String, String> extra
    ) {
        this.authDate = authDate;
        this.canSendAfter = canSendAfter;
        this.chat = chat;
        this.chatType = chatType;
        this.chatInstance = chatInstance;
        this.hash = hash;
        this.queryId = queryId;
        this.receiver = receiver;
        this.startParam = startParam;
        this.user = user;
        this.extra = extra == null ? Map.of() : extra;
    }

    public InitData(
            long authDate,
            @Nullable Long canSendAfter,
            @Nullable Chat chat,
            @Nullable ChatType chatType,
            @Nullable String chatInstance,
            @NotNull String hash,
            @Nullable String queryId,
            @Nullable User receiver,
            @Nullable String startParam,
            @Nullable User user
    ) {
        this(authDate, canSendAfter, chat, chatType, chatInstance, hash, queryId, receiver, startParam, user, null);
    }

    public long getAuthDate() { return authDate; }
    public @Nullable Long getCanSendAfter() { return canSendAfter; }
    public @Nullable Chat getChat() { return chat; }
    public @Nullable ChatType getChatType() { return chatType; }
    public @Nullable String getChatInstance() { return chatInstance; }
    public @NotNull String getHash() { return hash; }
    public @Nullable String getQueryId() { return queryId; }
    public @Nullable User getReceiver() { return receiver; }
    public @Nullable String getStartParam() { return startParam; }
    public @Nullable User getUser() { return user; }
    public @NotNull Map<String, String> getExtra() { return extra; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InitData)) return false;
        InitData initData = (InitData) o;
        return authDate == initData.authDate
                && Objects.equals(canSendAfter, initData.canSendAfter)
                && Objects.equals(chat, initData.chat)
                && chatType == initData.chatType
                && Objects.equals(chatInstance, initData.chatInstance)
                && Objects.equals(hash, initData.hash)
                && Objects.equals(queryId, initData.queryId)
                && Objects.equals(receiver, initData.receiver)
                && Objects.equals(startParam, initData.startParam)
                && Objects.equals(user, initData.user)
                && Objects.equals(extra, initData.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                authDate, canSendAfter, chat, chatType, chatInstance, hash, queryId, receiver, startParam, user, extra
        );
    }

    @Override
    public String toString() {
        return "InitData{" +
                "authDate=" + authDate +
                ", canSendAfter=" + canSendAfter +
                ", chat=" + chat +
                ", chatType=" + chatType +
                ", chatInstance='" + chatInstance + '\'' +
                ", hash='" + hash + '\'' +
                ", queryId='" + queryId + '\'' +
                ", receiver=" + receiver +
                ", startParam='" + startParam + '\'' +
                ", user=" + user +
                ", extra=" + extra +
                '}';
    }
}
