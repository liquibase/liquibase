package liquibase.migrator.preconditions;

import java.util.ArrayList;
import java.util.List;

import liquibase.migrator.Migrator;

public class PreconditionSet {

    private List<DBMSPrecondition> dbmsArray = new ArrayList<DBMSPrecondition>();
    private OrPrecondition orprecond;
    private NotPrecondition notprecond;
    private RunningAsPrecondition userexists;
    private Migrator migrator;
    private String strExceptionMsg;


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
        this.orprecond = orPre;

    }

    public OrPrecondition gerOrPreCondition() {
        return this.orprecond;
    }

    public void setNotPreCondition(NotPrecondition notPre) {
        this.notprecond = notPre;

    }

    public NotPrecondition getNotPreCondition() {
        return this.notprecond;
    }

    public void setRunningAs(RunningAsPrecondition userExi) {
        this.userexists = userExi;
    }

    public RunningAsPrecondition getRunningAs() {
        return this.userexists;
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
                        strExceptionMsg = "DBMS Precondition failed";
                        break;
                    }
                }

            } else if (orprecond != null) {
                if (orprecond.checkDbmsType(migrator)) {

                    orReturnValue = true;
                } else {
                    orReturnValue = false;
                    strExceptionMsg = "Or Precondition failed";
                }

            } else if (notprecond != null) {
                if (notprecond.checkNotPrecondition(migrator)) {

                    notReturnValue = true;
                } else {
                    notReturnValue = false;
                    strExceptionMsg = "Not Precondition failed";
                }

            }

            if (userexists != null) {
                if (userexists.checkUserName(migrator)) {

                    userExistsReturnValue = true;
                } else {
                    userExistsReturnValue = false;
                    strExceptionMsg = "UserExists Precondition failed";
                }

            }

            if (!(dbmsreturnvalue && orReturnValue && notReturnValue && userExistsReturnValue)) {
                throw new PreconditionFailedException();
            }

        } catch (PreconditionFailedException ePrecondExcep) {
            throw new PreconditionFailedException("Unable to process change set:" + strExceptionMsg);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
