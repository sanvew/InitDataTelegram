package io.github.sanvew.tg.init.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.sanvew.tg.init.data.exception.AuthDateInvalidException;
import io.github.sanvew.tg.init.data.exception.AuthDateMissingException;
import io.github.sanvew.tg.init.data.exception.ExpiredException;
import io.github.sanvew.tg.init.data.exception.SignatureMissingException;
import io.github.sanvew.tg.init.data.json.parser.InitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.json.parser.impl.JacksonInitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.ChatType;
import io.github.sanvew.tg.init.data.type.InitData;
import io.github.sanvew.tg.init.data.type.User;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for parsing and validating Telegram Mini App {@code initData} payloads.
 * <p>
 * Provides static methods to parse, validate, and verify the authenticity of Telegram init data,
 * including HMAC signature validation and expiration checking.
 * <p>
 * This class supports default parsing logic via Jackson and exposes overloads for custom parsers and clocks.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data Documentation</a>
 */
public class InitDataUtils {
    private static final byte[] SECRET_KEY_INPUT = "WebAppData".getBytes(StandardCharsets.UTF_8);

    private InitDataUtils() {}

    /**
     * Verifies the validity of the provided {@code initData} string using the algorithm defined
     * in the <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a> documentation.
     *
     * @param initData the initialization data string received from the Telegram Mini App
     * @param botToken the bot token associated with the Telegram bot
     * @param expiresIn optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                  if {@code null}, no expiration validation is performed
     * @param clock optional clock to use for time comparison; if {@code null}, the system default clock is used
     * @return {@code true} if the hash is valid and {@code auth_date} (if checked) is within the valid time range;
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateMissingException if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a>
     */
    public static boolean isValid(
            @NotNull String initData,
            @NotNull String botToken,
            @Nullable Duration expiresIn,
            @Nullable Clock clock
    ) {
        if (initData == null || initData.isBlank()) {
            throw buildExceptionArgumentNotProvided("initData");
        }
        if (botToken == null || botToken.isBlank()) {
            throw buildExceptionArgumentNotProvided("botToken");
        }

        final Map<String, String> parsedInitData = parseQueryString(initData);

        final String hashFromInitData = parsedInitData.remove(InitData.Param.HASH.value);
        if (hashFromInitData == null) {
            throw new SignatureMissingException();
        }

        if (expiresIn != null) {
            validateAuthDate(parsedInitData.get(InitData.Param.AUTH_DATE.value), expiresIn, clock);
        }

        final String formattedInitData = formattedInitData(parsedInitData);
        final byte[] secretKey = hmacDigest(botToken.getBytes(StandardCharsets.UTF_8), SECRET_KEY_INPUT);
        final byte[] computedHash = hmacDigest(formattedInitData.getBytes(StandardCharsets.UTF_8), secretKey);

        return bytesToHex(computedHash).equals(hashFromInitData);
    }

    /**
     * Verifies the validity of the provided {@code initData} using the default system clock
     * and optionally checks the {@code auth_date} against an expiration duration, as defined in the
     * <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a> documentation.
     * <p>
     * Internally delegates to {@link #isValid(String, String, Duration, Clock)} with {@code clock} set to {@code null}.
     *
     * @param initData the initialization data string received from the Telegram Mini App
     * @param botToken the bot token associated with the Telegram bot
     * @param expiresIn optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                  if {@code null}, no expiration validation is performed
     * @return {@code true} if the hash is valid and {@code auth_date} (if checked) is within the valid time range;
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateMissingException if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @see #isValid(String, String, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a>
     */
    public static boolean isValid(@NotNull String initData, @NotNull String botToken, @Nullable Duration expiresIn) {
        return InitDataUtils.isValid(initData, botToken, expiresIn, null);
    }

    /**
     * Verifies the validity of the provided {@code initData} using the default system clock
     * and without checking for expiration, as defined in the
     * <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a> documentation.
     * <p>
     * Internally delegates to {@link #isValid(String, String, Duration, Clock)}
     * with {@code expiresIn} and {@code clock} set to {@code null}.
     *
     * @param initData the initialization data string received from the Telegram Mini App
     * @param botToken the bot token associated with the Telegram bot
     * @return {@code true} if the hash is valid; {@code false} otherwise
     * @throws IllegalArgumentException if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @see #isValid(String, String, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a>
     */
    public static boolean isValid(@NotNull String initData, @NotNull String botToken) {
        return InitDataUtils.isValid(initData, botToken, null, null);
    }

    /**
     * Parses the provided {@code initData} string into an {@link InitData} object using the given {@code parser}.
     * The string must be in the format defined by Telegram Mini Apps and include at minimum an {@code auth_date} and {@code hash}.
     * <p>
     * If any optional values are not present in the string, they will be {@code null} in the resulting {@link InitData}.
     *
     * @param initData the raw init data string received from Telegram (must be URL query format)
     * @param parser optional parser to deserialize structured fields like {@code user} and {@code chat};
     *               if {@code null}, a default Jackson-based parser is used
     * @return parsed {@link InitData} object with typed fields
     * @throws IllegalArgumentException if {@code initData} is {@code null} or {@code isBlank() == true}
     * @throws NumberFormatException if {@code can_send_after} can't be parsed to {@link Long}
     * @throws io.github.sanvew.tg.init.data.exception.JsonParseException if there are occurred during json field parsing (e.g. {@code user}, {@code chat} etc.)
     * @throws io.github.sanvew.tg.init.data.exception.JsonPropertyMissingException if any required property in json object is missing
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateInvalidException if {@code auth_date} cannot be parsed into a valid timestamp
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data documentation</a>
     */
    public static @NotNull InitData parse(@NotNull String initData, @Nullable InitDataJsonTypesParser parser) {
        if (initData == null || initData.isBlank()) {
            throw buildExceptionArgumentNotProvided("initData");
        }

        if (parser == null) {
            parser = JacksonInitDataJsonTypesParser.INSTANCE;
        }

        final Map<String, String> parsedInitData = parseQueryString(initData);

        final Long authDate = parsedInitData.containsKey(InitData.Param.AUTH_DATE.value)
                ? parseAuthDate(parsedInitData.remove(InitData.Param.AUTH_DATE.value))
                : null;
        if (authDate == null) { throw new AuthDateMissingException(); }
        Long canSendAfter;
        try {
            canSendAfter = parsedInitData.containsKey(InitData.Param.CAN_SEND_AFTER.value)
                    ? Long.parseLong(parsedInitData.remove(InitData.Param.CAN_SEND_AFTER.value))
                    : null;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Unable to parse "
                    + InitData.Param.CAN_SEND_AFTER.value
                    + ": "
                    + parsedInitData.get(InitData.Param.CAN_SEND_AFTER.value)
            );
        }
        final Chat chat = parser.parseChat(parsedInitData.remove(InitData.Param.CHAT.value));
        final ChatType chatType = ChatType.fromValue(parsedInitData.remove(InitData.Param.CHAT_TYPE.value));
        final String chatInstance = parsedInitData.remove(InitData.Param.CHAT_INSTANCE.value);
        final String hash = parsedInitData.remove(InitData.Param.HASH.value);
        if (hash == null) { throw new SignatureMissingException(); }
        final String queryId = parsedInitData.remove(InitData.Param.QUERY_ID.value);
        final User receiver = parser.parseUser(parsedInitData.remove(InitData.Param.RECEIVER.value));
        final String startParam = parsedInitData.remove(InitData.Param.START_PARAM.value);
        final User user = parser.parseUser(parsedInitData.remove(InitData.Param.USER.value));

        return new InitData(
                authDate, canSendAfter, chat, chatType, chatInstance, hash, queryId, receiver, startParam, user,
                Map.copyOf(parsedInitData)
        );
    }

    /**
     * Parses the provided {@code initData} string into an {@link InitData} object using the default parser implementation.
     * <p>
     * Internally delegates to {@link #parse(String, InitDataJsonTypesParser)} with a default Jackson-based parser.
     *
     * @param initData the raw init data string received from Telegram (must be URL query format)
     * @return parsed {@link InitData} object with typed fields
     * @throws IllegalArgumentException if {@code initData} is {@code null} or {@code isBlank() == true}
     * @throws NumberFormatException if {@code can_send_after} can't be parsed to {@link Long}
     * @throws io.github.sanvew.tg.init.data.exception.JsonParseException if there are occurred during json field parsing (e.g. {@code user}, {@code chat} etc.)
     * @throws io.github.sanvew.tg.init.data.exception.JsonPropertyMissingException if any required property in json object is missing
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateInvalidException if {@code auth_date} cannot be parsed into a valid timestamp
     * @see #parse(String, InitDataJsonTypesParser)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data documentation</a>
     */
    public static @NotNull InitData parse(@NotNull String initData) {
        return InitDataUtils.parse(initData, null);
    }

    // =================================================================================================================
    // initData parsing
    // =================================================================================================================
    private static Map<String, String> parseQueryString(String queryString) {
        final Map<String, String> parameters = new TreeMap<>();
        final String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            final String value = idx > 0 && pair.length() > idx + 1
                    ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                    : null;
            parameters.put(key, value);
        }
        return parameters;
    }

    private static String formattedInitData(Map<String, String> initData) {
        final Map<String, String> sortedInitData = new TreeMap<>(initData);
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<String, String>> entriesIterator = sortedInitData.entrySet().iterator();
        while (entriesIterator.hasNext()) {
            final Map.Entry<String, String> entry = entriesIterator.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            if (entriesIterator.hasNext()) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    // =================================================================================================================
    // Hmac256 digest related methods
    // =================================================================================================================
    private static byte[] hmacDigest(byte[] data, byte[] key) {
        try {
            final Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"));
            return hmacSHA256.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        final StringBuilder strBuilder = new StringBuilder();
        for (byte b : bytes) {
            strBuilder.append(String.format("%02x", b));
        }
        return strBuilder.toString();
    }

    // =================================================================================================================
    // auth_date validation
    // =================================================================================================================
    private static long parseAuthDate(String authDate) {
        try {
            return Long.parseLong(authDate);
        } catch (NumberFormatException e) {
            throw new AuthDateInvalidException(authDate);
        }
    }

    private static void validateAuthDate(String authDate, Duration expiresIn, Clock clock) {
        if (authDate == null) {
            throw new AuthDateMissingException();
        }

        final Instant instantAuthDate = Instant.ofEpochSecond(parseAuthDate(authDate));
        final Instant instantNow = clock != null ? Instant.now(clock) : Instant.now();

        final Instant expiration = instantAuthDate.plus(expiresIn);
        if (instantNow.isAfter(expiration)) {
            throw new ExpiredException(instantAuthDate.getEpochSecond(), instantNow.getEpochSecond());
        }
    }

    // =================================================================================================================
    // misc methods
    // =================================================================================================================
    private static IllegalArgumentException buildExceptionArgumentNotProvided(String argument) {
        return new IllegalArgumentException("Argument \"" + argument + "\" is null or empty!");
    }
}
