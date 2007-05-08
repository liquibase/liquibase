package liquibase.migrator.preconditions;

import liquibase.migrator.Migrator;

import java.util.ArrayList;
import java.util.List;

public class PreconditionSet {

    private List<DBMSPrecondition> dbmsArray = new ArrayList<DBMSPrecondition>();
    private OrPrecondition or;
    private NotPrecondition not;
    private RunningAsPrecondition runningAs;
    private Migrator migrator;
    private String exceptionMsg;


    public PreconditionSet() {
        migrator = null;
    }

    public PreconditionSet(Migrator aMigrator) {
        this.migrator = aMigrator;
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

        try {

            if (dbmsArray.size() > 0) {
                for (int i = 0; i < dbmsArray.size(); i++) {
                    DBMSPrecondition dbmsPrecondition = dbmsArray.get(i);
                    if (dbmsPrecondition.checkDatabaseType(migrator)) {
                        dbmsreturnvalue = true;
                    } else {
                        dbmsreturnvalue = false;
                        exceptionMsg = "DBMS Precondition failed";
                        break;
                    }
                }

            } else if (or != null) {
                if (or.checkDbmsType(migrator)) {

                    orReturnValue = true;
                } else {
                    orReturnValue = false;
                    exceptionMsg = "Or Precondition failed";
                }

            } else if (not != null) {
                if (not.checkNotPrecondition(migrator)) {

                    notReturnValue = true;
                } else {
                    notReturnValue = false;
                    exceptionMsg = "Not Precondition failed";
                }

            }

            if (runningAs != null) {
                if (runningAs.checkUserName(migrator)) {

                    userExistsReturnValue = true;
                } else {
                    userExistsReturnValue = false;
                    exceptionMsg = "UserExists Precondition failed";
                }

            }

            if (!(dbmsreturnvalue && orReturnValue && notReturnValue && userExistsReturnValue)) {
                throw new PreconditionFailedException();
            }

        } catch (PreconditionFailedException ePrecondExcep) {
            throw new PreconditionFailedException("Unable to process change set: " + exceptionMsg);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
