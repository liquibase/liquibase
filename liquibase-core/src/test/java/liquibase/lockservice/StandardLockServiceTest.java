package liquibase.lockservice;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StandardLockServiceTest {

    public static final LocalDateTime LOCKGRANTED_DATETIME = LocalDateTime.of(2021, 6, 1, 20, 34);
    public static final Date LOCKGRANTED_DATE = Date.from(Instant.ofEpochMilli(1622580938000l));
    public static final int ID0 = 23;
    public static final int ID1 = 42;
    public static final String LOCKEDBY = "The devil";
    private StandardLockService lockService;
    private Scope mockedScope;

    @Before
    public void before() throws Exception {
        Executor executor = Mockito.mock(Executor.class);
        Mockito
            .when(executor.queryForList(Mockito.any()))
            .thenReturn(sampleLockData());
        ExecutorService executorService = Mockito.mock(ExecutorService.class);
        Mockito
            .when(executorService.getExecutor(Mockito.any(), Mockito.any()))
            .thenReturn(executor);

        Map<String, Object> scopeValues = new TreeMap<>();

        scopeValues.put(ExecutorService.class.getName(), executorService);

        mockedScope = Mockito.mock(Scope.class);
        Mockito.when(mockedScope.getSingleton(ExecutorService.class)).thenReturn(executorService);

        lockService = new StandardLockService() {
            @Override
            public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
                return true;
            }
        };
    }

    @Test
    public void shouldAcceptLocaldatetimeInLOCKGRANTED() throws LockException {
        try (MockedStatic<Scope> scope = Mockito.mockStatic(Scope.class)) {
            scope.when(Scope::getCurrentScope).thenReturn(mockedScope);
            DatabaseChangeLogLock[] databaseChangeLogLocks = lockService.listLocks();

            Assertions.assertThat(databaseChangeLogLocks)
                .hasSize(2);

            Assertions.assertThat(databaseChangeLogLocks[0].getId())
                .isEqualTo(ID0);
            Assertions.assertThat(databaseChangeLogLocks[0].getLockGranted())
                .isEqualTo(Date.from((LOCKGRANTED_DATETIME).atZone(ZoneId.systemDefault()).toInstant()));
            Assertions.assertThat(databaseChangeLogLocks[0].getLockedBy())
                .isEqualTo(LOCKEDBY);

            Assertions.assertThat(databaseChangeLogLocks[1].getId())
                .isEqualTo(ID1);
            Assertions.assertThat(databaseChangeLogLocks[1].getLockGranted())
                .isEqualTo(LOCKGRANTED_DATE);
            Assertions.assertThat(databaseChangeLogLocks[1].getLockedBy())
                .isEqualTo(LOCKEDBY);
        }
    }

    private static List<Map<String, ?>> sampleLockData() {
        Map<String, Object> columnMapRow0 = new TreeMap<>();

        columnMapRow0.put("ID", ID0);
        columnMapRow0.put("LOCKED", true);
        columnMapRow0.put("LOCKGRANTED", LOCKGRANTED_DATETIME);
        columnMapRow0.put("LOCKEDBY", LOCKEDBY);

        Map<String, Object> columnMapRow1 = new TreeMap<>();

        columnMapRow1.put("ID", ID1);
        columnMapRow1.put("LOCKED", 1);
        columnMapRow1.put("LOCKGRANTED", LOCKGRANTED_DATE);
        columnMapRow1.put("LOCKEDBY", LOCKEDBY);

        return Arrays.asList(columnMapRow0, columnMapRow1);
    }
}
