package liquibase.lockservice;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.PostgresDatabase;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Verifies the opt-in gating: {@link PostgreSQLSessionLockService} is only selectable when
 * {@code liquibase.useSessionLock} is enabled AND the database is PostgreSQL 9.1+, and that it
 * outranks {@link StandardLockService} so Liquibase picks it once enabled. Default behaviour
 * (flag off) is unchanged.
 */
public class PostgreSQLSessionLockServiceTest {

    private final PostgreSQLSessionLockService lockService = new PostgreSQLSessionLockService();

    @Test
    public void notSupportedByDefaultEvenOnPostgres() {
        assertThat(lockService.supports(postgres(14, 0))).isFalse();
    }

    @Test
    public void supportedOnPostgres91PlusWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(postgres(14, 0))).isTrue();
            assertThat(lockService.supports(postgres(9, 1))).isTrue();
        });
    }

    @Test
    public void notSupportedOnOlderPostgresEvenWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(postgres(9, 0))).isFalse();
        });
    }

    @Test
    public void notSupportedOnOtherDatabasesEvenWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(new MockDatabase())).isFalse();
        });
    }

    @Test
    public void outranksStandardLockService() {
        assertThat(lockService.getPriority()).isGreaterThan(new StandardLockService().getPriority());
    }

    private static PostgresDatabase postgres(int majorVersion, int minorVersion) {
        try {
            PostgresDatabase database = Mockito.mock(PostgresDatabase.class);
            Mockito.when(database.getDatabaseMajorVersion()).thenReturn(majorVersion);
            Mockito.when(database.getDatabaseMinorVersion()).thenReturn(minorVersion);
            return database;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
