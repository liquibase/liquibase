package liquibase.migrator.preconditions;

import java.util.ArrayList;
import java.util.List;

import liquibase.migrator.Migrator;

public class NotPrecondition implements PreconditionLogic {
    private List<DBMSPrecondition> dbmsArray = new ArrayList<DBMSPrecondition>();
    private OrPrecondition orprecondition;

    public void setOrprecondition(OrPrecondition precond) {
        this.orprecondition = precond;

    }

    public OrPrecondition getOrprecondition() {

        return this.orprecondition;
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

    public boolean checkNotPrecondition(Migrator migrator) {
        boolean booldbmsRetVal = true;
        boolean boolOrRetVal = true;
        boolean returnVal = true;
        if (dbmsArray.size() != 0) {
            booldbmsRetVal = checkDbmsType(migrator);
        }
        if (orprecondition != null) {
            boolOrRetVal = orprecondition.checkDbmsType(migrator);
            boolOrRetVal = ! boolOrRetVal;

        }
        if (boolOrRetVal && booldbmsRetVal) {
            returnVal = true;

        } else {

            returnVal = false;
        }

        return returnVal;

    }

    public boolean checkDbmsType(Migrator migrator) {
        boolean returnvalue = true;
        boolean boolReturnArray[] = new boolean[dbmsArray.size()];
        try {
            for (int i = 0; i < dbmsArray.size(); i++) {
                DBMSPrecondition dbmsPrecondition = dbmsArray.get(i);
                //String dbproduct = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();
                if (dbmsPrecondition.checkDatabaseType(migrator)) {
                    boolReturnArray[i] = true;
                } else {

                    boolReturnArray[i] = false;
                }

            }

            for (int i = 0; i < dbmsArray.size(); i++) {

                returnvalue = returnvalue && boolReturnArray[i];


            }
            returnvalue = ! returnvalue;


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return returnvalue;

    }
}
