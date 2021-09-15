package liquibase.integration.spring;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SpringLiquibase}
 */
public class SpringLiquibaseTest {

    public static final String TEST_CONTEXT = "test_context";
    public static final String TEST_LABELS = "test_labels";
    public static final String TEST_TAG = "test_tag";

    private SpringLiquibase springLiquibase = new SpringLiquibase();

    private Liquibase liquibase;

    private ArgumentCaptor<Contexts> contextCaptor;
    private ArgumentCaptor<LabelExpression> labelCaptor;
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void setUp() {
        liquibase = mock(Liquibase.class);
        springLiquibase.setContexts(TEST_CONTEXT);
        springLiquibase.setLabels(TEST_LABELS);
        contextCaptor = ArgumentCaptor.forClass(Contexts.class);
        labelCaptor = ArgumentCaptor.forClass(LabelExpression.class);
        stringCaptor = ArgumentCaptor.forClass(String.class);
    }

    @Test
    public void testRollbackOnUpdateToFalse() throws Exception {
        springLiquibase.setTestRollbackOnUpdate(false);

        springLiquibase.performUpdate(liquibase);

        verify(liquibase).update(contextCaptor.capture(), labelCaptor.capture());
        assertSame(contextCaptor.getValue().getContexts().size(), 1);
        assertTrue(contextCaptor.getValue().getContexts().contains(TEST_CONTEXT));
        assertSame(labelCaptor.getValue().getLabels().size(), 1);
        assertTrue(labelCaptor.getValue().getLabels().contains(TEST_LABELS));
    }

    @Test
    public void testRollbackOnUpdateToFalseWithTag() throws Exception {
        springLiquibase.setTag(TEST_TAG);
        springLiquibase.setTestRollbackOnUpdate(false);

        springLiquibase.performUpdate(liquibase);

        verify(liquibase).update(stringCaptor.capture(), contextCaptor.capture(), labelCaptor.capture());
        assertSame(contextCaptor.getValue().getContexts().size(), 1);
        assertTrue(contextCaptor.getValue().getContexts().contains(TEST_CONTEXT));
        assertSame(labelCaptor.getValue().getLabels().size(), 1);
        assertTrue(labelCaptor.getValue().getLabels().contains(TEST_LABELS));
        assertSame(stringCaptor.getValue(), TEST_TAG);
    }

    @Test
    public void testRollbackOnUpdateToTrue() throws Exception {
        springLiquibase.setTestRollbackOnUpdate(true);

        springLiquibase.performUpdate(liquibase);

        verify(liquibase).updateTestingRollback(contextCaptor.capture(), labelCaptor.capture());
        assertSame(contextCaptor.getValue().getContexts().size(), 1);
        assertTrue(contextCaptor.getValue().getContexts().contains(TEST_CONTEXT));
        assertSame(labelCaptor.getValue().getLabels().size(), 1);
        assertTrue(labelCaptor.getValue().getLabels().contains(TEST_LABELS));
    }

    @Test
    public void testRollbackOnUpdateToTrueWithTag() throws Exception {
        springLiquibase.setTag(TEST_TAG);
        springLiquibase.setTestRollbackOnUpdate(true);

        springLiquibase.performUpdate(liquibase);

        verify(liquibase).updateTestingRollback(stringCaptor.capture(), contextCaptor.capture(), labelCaptor.capture());
        assertSame(contextCaptor.getValue().getContexts().size(), 1);
        assertTrue(contextCaptor.getValue().getContexts().contains(TEST_CONTEXT));
        assertSame(labelCaptor.getValue().getLabels().size(), 1);
        assertTrue(labelCaptor.getValue().getLabels().contains(TEST_LABELS));
        assertSame(stringCaptor.getValue(), TEST_TAG);
    }

    @Test
    public void testUpdateNotRunWhenNotNeeded() throws Exception {
        springLiquibase.performUpdateIfNeeded(liquibase);

        verify(liquibase, never()).update(any(String.class), any(Contexts.class), any(LabelExpression.class));
        verify(liquibase, never()).updateTestingRollback(any(String.class), any(Contexts.class), any(LabelExpression.class));
    }

    @Test
    public void testUpdateRunWhenNeeded() throws Exception {
        DatabaseChangeLog changelog = new DatabaseChangeLog();
        ChangeSet changeSet = new ChangeSet(changelog);
        changelog.addChangeSet(changeSet);
        List<ChangeSet> changeSets = new ArrayList<>();
        changeSets.add(changeSet);
        when(liquibase.listUnrunChangeSets(any(Contexts.class), any(LabelExpression.class))).thenReturn(changeSets);
        ChangeLogHistoryService changeLogHistoryServiceMock = mock(ChangeLogHistoryService.class);
        mockChangeLogHistoryService(changeLogHistoryServiceMock);

        springLiquibase.performUpdateIfNeeded(liquibase);

        verify(liquibase).update(any(Contexts.class), any(LabelExpression.class));
        verify(changeLogHistoryServiceMock).reset();
        ChangeLogHistoryServiceFactory.reset();
    }

    private void mockChangeLogHistoryService(ChangeLogHistoryService changeLogHistoryServiceMock) {
        ChangeLogHistoryServiceFactory changeLogHistoryServiceFactoryMock = mock(ChangeLogHistoryServiceFactory.class);
        when(changeLogHistoryServiceFactoryMock.getChangeLogService(any())).thenReturn(changeLogHistoryServiceMock);
        ChangeLogHistoryServiceFactory.setInstance(changeLogHistoryServiceFactoryMock);
    }
}
