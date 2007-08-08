package liquibase.migrator.preconditions;

import liquibase.migrator.DatabaseChangeLog;
import liquibase.migrator.Migrator;
import liquibase.migrator.exception.PreconditionFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for all preconditions on a change log.
 */
public class PreconditionSet {

    private List<DBMSPrecondition> dbmsArray = new ArrayList<DBMSPrecondition>();
    private OrPrecondition or;
    private NotPrecondition not;
    private RunningAsPrecondition runningAs;
    private Migrator migrator;
    private DatabaseChangeLog changeLog;


    public PreconditionSet() {
        migrator = null;
    }

    public PreconditionSet(Migrator aMigrator) {
        this.migrator = aMigrator;
    }

    public PreconditionSet(Migrator migrator, DatabaseChangeLog changeLog) {
        this.migrator = migrator;
        this.changeLog = changeLog;
    }

    public void setDbmsArray(List<DBMSPrecondition> dbmsArr) {
        this.dbmsArray = dbmsArr;
    }

    public List<DBMSPrecondition> getDbmsArray() {
        return this.dbmsArray;
    }

    public void addDbms(DBMSPrecondition dbmsPrecondition) {
        dbmsArray.add(dbmsPrecondition);
    }

    public void setOrPreCondition(OrPrecondition orPre) {
        this.or = orPre;

    }

    public OrPrecondition gerOrPreCondition() {
        return this.or;
    }

    public void setNotPreCondition(NotPrecondition notPre) {
        this.not = notPre;

    }

    public NotPrecondition getNotPreCondition() {
        return this.not;
    }

    public void setRunningAs(RunningAsPrecondition userExi) {
        this.runningAs = userExi;
    }

    public RunningAsPrecondition getRunningAs() {
        return this.runningAs;
    }

    public void checkConditions() throws PreconditionFailedException {

        boolean dbmsreturnvalue = true;
        boolean orReturnValue = true;
        boolean notReturnValue = true;
        boolean userExistsReturnValue = true;

        List<FailedPrecondition> failedPreconditions = new ArrayList<FailedPrecondition>();

        try {
            if (dbmsArray.size() > 0) {
                for (int i = 0; i < dbmsArray.size(); i++) {
                    DBMSPrecondition dbmsPrecondition = dbmsArray.get(i);
                    if (dbmsPrecondition.checkDatabaseType(migrator)) {
                        dbmsreturnvalue = true;
                    } else {
                        dbmsreturnvalue = false;
                        failedPreconditions.add(new FailedPrecondition("DBMS Precondition failed", changeLog, dbmsPrecondition));
                        break;
                    }
                }

            } else if (or != null) {
                if (or.checkDbmsType(migrator)) {

                    orReturnValue = true;
                } else {
                    orReturnValue = false;
                    failedPreconditions.add(new FailedPrecondition("Or Precondition failed", changeLog, or));
                }

            } else if (not != null) {
                if (not.checkNotPrecondition(migrator)) {

                    notReturnValue = true;
                } else {
                    notReturnValue = false;
                    failedPreconditions.add(new FailedPrecondition("Not Precondition failed", changeLog, not));
                }

            }

            if (runningAs != null) {
                if (runningAs.checkUserName(migrator)) {

                    userExistsReturnValue = true;
                } else {
                    userExistsReturnValue = false;
                    failedPreconditions.add(new FailedPrecondition("runningAs Precondition failed", changeLog, runningAs));
                }

            }

            if (!(dbmsreturnvalue && orReturnValue && notReturnValue && userExistsReturnValue)) {
                throw new PreconditionFailedException(failedPreconditions);
            }

        } catch (PreconditionFailedException ePrecondExcep) {
            throw ePrecondExcep;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
