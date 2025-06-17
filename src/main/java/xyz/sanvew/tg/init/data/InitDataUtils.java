package xyz.sanvew.tg.init.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.sanvew.tg.init.data.exception.AuthDateInvalidException;
import xyz.sanvew.tg.init.data.exception.AuthDateMissingException;
import xyz.sanvew.tg.init.data.exception.ExpiredException;
import xyz.sanvew.tg.init.data.exception.SignatureMissingException;

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
        if (initData == null) {
            throw buildNotProvidedIllegalArgumentException("initData");
        }
        if (botToken == null) {
            throw buildNotProvidedIllegalArgumentException("botToken");
        }

        final Map<String, String> parsedInitData = parseQueryString(initData);

        final String hashFromInitData = parsedInitData.remove("hash");
        if (hashFromInitData == null) {
            throw new SignatureMissingException();
        }

        if (expiresIn != null) {
            validateAuthDate(parsedInitData.get("auth_date"), expiresIn, clock);
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

//    /**
//     * TODO
//     * @param initData
//     * @return
//     */
//    @NotNull
//    public static InitData parse(@NotNull String initData) {
//        throw new UnsupportedOperationException("Not yet implemented!");
//    }

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
    public static byte[] hmacDigest(byte[] data, byte[] key) {
        try {
            final Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"));
            return hmacSHA256.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        final StringBuilder strBuilder = new StringBuilder();
        for (byte b : bytes) {
            strBuilder.append(String.format("%02x", b));
        }
        return strBuilder.toString();
    }

    // =================================================================================================================
    // auth_date validation
    // =================================================================================================================
    public static void validateAuthDate(String authDate, Duration expiresIn, Clock clock) {
        if (authDate == null) {
            throw new AuthDateMissingException();
        }

        long authDateLong;
        try {
            authDateLong = Long.parseLong(authDate);
        } catch (NumberFormatException e) {
            throw new AuthDateInvalidException();
        }

        final Instant instantNow = clock != null ? Instant.now(clock) : Instant.now();

        final Instant expiration = Instant.ofEpochSecond(authDateLong).plus(expiresIn);
        if (instantNow.isAfter(expiration)) {
            throw new ExpiredException();
        }
    }

    // =================================================================================================================
    // misc methods
    // =================================================================================================================
    private static IllegalArgumentException buildNotProvidedIllegalArgumentException(String argumentName) {
        return new IllegalArgumentException("Argument \"" + argumentName + "\" is null!");
    }
}
