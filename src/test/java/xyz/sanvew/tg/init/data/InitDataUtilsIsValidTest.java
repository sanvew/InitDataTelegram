package xyz.sanvew.tg.init.data;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import xyz.sanvew.tg.init.data.exception.AuthDateInvalidException;
import xyz.sanvew.tg.init.data.exception.AuthDateMissingException;
import xyz.sanvew.tg.init.data.exception.ExpiredException;
import xyz.sanvew.tg.init.data.exception.SignatureMissingException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class InitDataUtilsIsValidTest {
    final long STUB_AUTH_DATE = 1749945600;
    final String STUB_BOT_TOKEN = "123456789:TEST_FAKE_BOT_TOKEN_EXAMPLE123456";
    final String STUB_VALID_INIT_DATA = "auth_date=" + STUB_AUTH_DATE +
            "&chat_type=group" +
            "&query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
            "&start_param=referral123" +
            "&user=%7B%22id%22%3A123456789%2C%22first_name%22%3A%22John%22%2C%22last_name%22%3A%22Doe%22%2C%22username%22%3A%22johndoe%22%2C%22language_code%22%3A%22en%22%2C%22is_bot%22%3Afalse%2C%22is_premium%22%3Atrue%2C%22allows_write_to_pm%22%3Atrue%2C%22photo_url%22%3A%22https%3A%2F%2Fexample.com%2Favatar%2F4843.jpg%22%7D" +
            "&hash=d88ca7df91a7a28bb3b34857ed9e0ec4d99dfa2bf81fd9321e21e3abf84a8ae3";

    @Test
    void isValid_withOfficialDocumentationExample_returnTrue() {
        final String tgOffDocBotToken = "5768337691:AAH5YkoiEuPk8-FZa32hStHTqXiLPtAEhx8";
        final String tgOffDocInitData = "query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
                "&user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%7D" +
                "&auth_date=1662771648" +
                "&hash=c501b71e775f74ce10e377dea85a7ea24ecd640b223ea86dfe453e0eaed2e2b2";

        assertTrue(InitDataUtils.isValid(tgOffDocInitData, tgOffDocBotToken));
    }

    @Test
    void isValid_withNullArguments_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> InitDataUtils.isValid(null, STUB_BOT_TOKEN));

        assertThrows(IllegalArgumentException.class, () -> InitDataUtils.isValid(STUB_VALID_INIT_DATA, null));
    }

    @Test
    void isValid_withMissingHash_throwsSignatureMissingException() {
        final String initDataMissingHash = "auth_date=" + STUB_AUTH_DATE;
        assertThrows(SignatureMissingException.class, () ->
                InitDataUtils.isValid(initDataMissingHash, STUB_BOT_TOKEN)
        );
    }

    @Test
    void isValid_withMissingAuthDate_throwsAuthDateMissingException() {
        final String initDataMissingAuthDate = "hash=dummyhash";
        assertThrows(AuthDateMissingException.class, () ->
                InitDataUtils.isValid(initDataMissingAuthDate, STUB_BOT_TOKEN, Duration.ofMinutes(5))
        );
    }

    @Test
    void isValid_withInvalidAuthDateFormat_throwsAuthDateInvalidException() {
        final String initDataInvalidAuthDate = "auth_date=invalid&hash=dummyhash";
        assertThrows(AuthDateInvalidException.class, () ->
                InitDataUtils.isValid(initDataInvalidAuthDate, STUB_BOT_TOKEN, Duration.ofMinutes(5)));
    }

    @Test
    void isValid_withValidInitData_returnsTrue() {
        assertTrue(InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN));
    }

    @Test
    void isValid_withTamperedHash_returnsFalse() {
        final String DIFFERENT_STUB_BOT_TOKEN = "123456789:DIFFERENT_TEST_FAKE_BOT_TOKEN_EXAMPLE123456";
        assertFalse(InitDataUtils.isValid(STUB_VALID_INIT_DATA, DIFFERENT_STUB_BOT_TOKEN));
    }

    @Test
    void isValid_withExpiresInBeforeExpiration_returnsTrue() {
        final Duration providedExpiresIn = Duration.ofHours(2);
        final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1));

        try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

            assertTrue(InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn));
        }
    }

    @Test
    void isValid_withExpiresInAtExpirationBoundary_returnsTrue() {
        final Duration providedExpiresIn = Duration.ofHours(2);
        final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn);

        try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

            assertTrue(InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn));
        }
    }

    @Test
    void isValid_withExpiresInJustExpired_throwsExpiredException() {
        final Duration providedExpiresIn = Duration.ofHours(2);
        final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn).plusSeconds(1);

        try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

            assertThrows(ExpiredException.class, () ->
                    InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn)
            );
        }
    }

    @Test
    void isValid_withClockBeforeExpiration_returnsTrue() {
        final Clock fixedClock = Clock.fixed(
                Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofMinutes(30)),
                ZoneOffset.UTC
        );
        assertTrue(InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), fixedClock));
    }

    @Test
    void isValid_withClockAtExpirationBoundary_returnsTrue() {
        final Clock boundaryClock = Clock.fixed(
                Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1)),
                ZoneOffset.UTC
        );
        assertTrue(InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), boundaryClock));
    }

    @Test
    void isValid_withClockAfterExpiration_throwsExpiredException() {
        final Clock expiredClock = Clock.fixed(
                Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1)).plusSeconds(1),
                ZoneOffset.UTC
        );
        assertThrows(Exception.class, () ->
                InitDataUtils.isValid(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), expiredClock));
    }

}