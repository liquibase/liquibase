package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UnexpectedChangeSetsValidatorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Liquibase liquibase;

    @Mock
    private DatabaseChangeLog databaseChangeLog;

    private UnexpectedChangeSetsValidator<Liquibase> validator;

    @Before
    public void setUp() throws LiquibaseException {
        validator = new UnexpectedChangeSetsValidator<>();
        when(liquibase.getDatabaseChangeLog()).thenReturn(databaseChangeLog);
    }

    @Test
    public void detects_no_unexpected_change_set() throws Exception {
        ChangeSet changeSet1 = new ChangeSet("id1", "author1", true, true, "filePath1", null, null, databaseChangeLog);
        ChangeSet changeSet2 = new ChangeSet("id2", "author2", true, true, "filePath2", null, null, databaseChangeLog);

        when(liquibase.listUnexpectedChangeSets(new Contexts(), new LabelExpression())).thenReturn(Collections.emptyList());
        when(databaseChangeLog.getChangeSets()).thenReturn(Arrays.asList(
                changeSet1,
                changeSet2));
        validator.accept(liquibase);
        validator.validate(unexpected -> {
            throw new AssertionError(unexpected.toString());
        });
    }

    @Test
    public void detects_unexpected_change_set() throws Exception {
        ChangeSet changeSet1 = new ChangeSet("id1", "author1", true, true, "filePath1", null, null, databaseChangeLog);
        ChangeSet changeSet2 = new ChangeSet("id2", "author2", true, true, "filePath2", null, null, databaseChangeLog);
        ChangeSet changeSet3 = new ChangeSet("id3", "author3", true, true, "filePath3", null, null, null);

        when(liquibase.listUnexpectedChangeSets(any(), any())).thenReturn(Collections.singletonList(new RanChangeSet(changeSet3)));
        when(databaseChangeLog.getChangeSets()).thenReturn(Arrays.asList(
                changeSet1,
                changeSet2));
        validator.accept(liquibase);
        assertThatThrownBy(() -> validator.validate(unexpected -> {
            throw new IllegalStateException(unexpected.toString());
        })).isInstanceOf(IllegalStateException.class).hasMessageContaining("id3");
    }

    @Test
    public void can_filter_change_sets_of_multiple_instances() throws Exception {
        ChangeSet changeSet1 = new ChangeSet("id1", "author1", true, true, "filePath1", null, null, databaseChangeLog);
        ChangeSet changeSet2 = new ChangeSet("id2", "author2", true, true, "filePath2", null, null, databaseChangeLog);
        ChangeSet changeSet3 = new ChangeSet("id3", "author3", true, true, "filePath3", null, null, databaseChangeLog);
        ChangeSet changeSet4 = new ChangeSet("id4", "author4", true, true, "filePath4", null, null, databaseChangeLog);

        when(liquibase.listUnexpectedChangeSets(new Contexts(), new LabelExpression())).thenReturn(Arrays.asList(
                new RanChangeSet(changeSet1),
                new RanChangeSet(changeSet2)));
        when(databaseChangeLog.getChangeSets()).thenReturn(Arrays.asList(
                changeSet3,
                changeSet4));
        validator.accept(liquibase);

        Mockito.reset(liquibase, databaseChangeLog);
        when(liquibase.getDatabaseChangeLog()).thenReturn(databaseChangeLog);

        when(liquibase.listUnexpectedChangeSets(new Contexts(), new LabelExpression())).thenReturn(Arrays.asList(
                new RanChangeSet(changeSet2),
                new RanChangeSet(changeSet3)));
        when(databaseChangeLog.getChangeSets()).thenReturn(Arrays.asList(
                changeSet1,
                changeSet2));
        validator.accept(liquibase);

        validator.validate(unexpected -> {
            throw new AssertionError(unexpected.toString());
        });
    }
}
