package liquibase.hub.model

import liquibase.ContextExpression
import liquibase.Labels
import liquibase.change.CheckSum
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.RanChangeSet
import spock.lang.Specification

import static liquibase.changelog.ChangeSet.ExecType.EXECUTED

class HubChangeTest extends Specification {

    private static final String ID = "id"
    private static final String AUTHOR = "author"
    private static final boolean alwaysRun = false
    private static final boolean runOnChange = false
    private static final String PATH = "path"
    private static final String CONTEXT_LIST = "contextList"
    private static final String DBMS_LIST = "dbmsList"
    private static final String DESCRIPTION = "desc"
    private static final String COMMENTS = "comments"
    private static final String TAG = "tag"
    private static final String VERSION = "4.4.0"
    private static final String LABEL = "label"
    private static final int ORDER_EXECUTED = 1
    private static final String CHECK_SUM = "7:2cdf9876e74347162401315d34b83746"
    private static final String DEPLOYMENT_ID = "deploymentId"
    private static final Date DATE_EXECUTED = new Date()

    def "map RanChangeSet all fields set"() {
        given:
        def ranChangeSet = new RanChangeSet(PATH, ID, AUTHOR, CheckSum.parse(CHECK_SUM),
                DATE_EXECUTED, TAG, EXECUTED, DESCRIPTION, COMMENTS,
                new ContextExpression("context1", "context2"), new Labels(LABEL), DEPLOYMENT_ID)
        ranChangeSet.setLiquibaseVersion(VERSION)
        ranChangeSet.setOrderExecuted(ORDER_EXECUTED)

        when:
        def hubChange = new HubChange(ranChangeSet)

        then:
        hubChange.getChangesetId() == ID
        hubChange.getChangesetAuthor() == AUTHOR
        hubChange.getChangesetFilename() == PATH
        hubChange.getDescription() == DESCRIPTION
        hubChange.getComments() == COMMENTS
        hubChange.getTag() == TAG
        hubChange.getLiquibase() == VERSION
        hubChange.getOrderExecuted() == ORDER_EXECUTED
        hubChange.getExecType() == EXECUTED.name()
        hubChange.getDeploymentId() == DEPLOYMENT_ID
        hubChange.getDateExecuted() == DATE_EXECUTED
        hubChange.getLabels() == LABEL
        hubChange.getContexts() == "(context1), (context2)"
        hubChange.getMd5sum() == CHECK_SUM
    }

    def "map RanChangeSet if checkSum, labels and contexts are null then maps to null"() {
        given:
        def ranChangeSet = new RanChangeSet(PATH, ID, AUTHOR, null,
                DATE_EXECUTED, TAG, EXECUTED, DESCRIPTION, COMMENTS, null, null, DEPLOYMENT_ID)
        ranChangeSet.setLiquibaseVersion(VERSION)
        ranChangeSet.setOrderExecuted(ORDER_EXECUTED)

        when:
        def hubChange = new HubChange(ranChangeSet)

        then:
        hubChange.getChangesetId() == ID
        hubChange.getChangesetAuthor() == AUTHOR
        hubChange.getChangesetFilename() == PATH
        hubChange.getDescription() == DESCRIPTION
        hubChange.getComments() == COMMENTS
        hubChange.getTag() == TAG
        hubChange.getLiquibase() == VERSION
        hubChange.getOrderExecuted() == ORDER_EXECUTED
        hubChange.getExecType() == EXECUTED.name()
        hubChange.getDeploymentId() == DEPLOYMENT_ID
        hubChange.getDateExecuted() == DATE_EXECUTED
        hubChange.getLabels() == null
        hubChange.getContexts() == null
        hubChange.getMd5sum() == null
    }

    def "map ChangeSet all fields set (checkSum can't be null)"() {
        given:
        def changeSet = new ChangeSet(ID, AUTHOR, alwaysRun, runOnChange,
                PATH, CONTEXT_LIST, DBMS_LIST, new DatabaseChangeLog())
        changeSet.setLabels(new Labels(LABEL))
        changeSet.setComments(COMMENTS)
        changeSet.clearCheckSum()

        when:
        def hubChange = new HubChange(changeSet)

        then:
        hubChange.getChangesetId() == ID
        hubChange.getChangesetAuthor() == AUTHOR
        hubChange.getChangesetFilename() == PATH
        hubChange.getComments() == COMMENTS
        hubChange.getDateExecuted() != null
        hubChange.getLabels() == LABEL
        hubChange.getContexts() == CONTEXT_LIST
        hubChange.getDescription() == "empty"
        // Checksum is generated automatically in the getter even if it is null
        hubChange.getMd5sum() == "9:d41d8cd98f00b204e9800998ecf8427e"
        // Constants
        hubChange.getOrderExecuted() == 0
        hubChange.getExecType() == "EXECUTED"
    }

    def "map ChangeSet if labels is null then maps labels to null and context to ()"() {
        given:
        def changeSet = new ChangeSet(ID, AUTHOR, alwaysRun, runOnChange,
                PATH, null, DBMS_LIST, new DatabaseChangeLog())
        changeSet.setComments(COMMENTS)
        changeSet.clearCheckSum()

        when:
        def hubChange = new HubChange(changeSet)

        then:
        hubChange.getChangesetId() == ID
        hubChange.getChangesetAuthor() == AUTHOR
        hubChange.getChangesetFilename() == PATH
        hubChange.getComments() == COMMENTS
        hubChange.getDateExecuted() != null
        hubChange.getDescription() == "empty"
        hubChange.getLabels() == null
        // Default value returned from ContextExpression.toString() if no contexts
        hubChange.getContexts() == "()"
        // Checksum is generated automatically in the getter even if it is null
        hubChange.getMd5sum() == "9:d41d8cd98f00b204e9800998ecf8427e"
        // Constants
        hubChange.getOrderExecuted() == 0
        hubChange.getExecType() == "EXECUTED"
    }
}
