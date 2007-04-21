package liquibase.migrator.preconditions;

import java.util.ArrayList;
import java.util.List;

import liquibase.migrator.Migrator;

public class OrPrecondition implements PreconditionLogic{
    private List<DBMSPrecondition> dbmsArray = new ArrayList<DBMSPrecondition>();

    public void setDbmsArray(List<DBMSPrecondition> dbmsArr) {
        this.dbmsArray = dbmsArr;

    }

    public List getDbmsArray() {
        return this.dbmsArray;
    }

    public void addDbms(DBMSPrecondition dbmsPrecondition) {
        dbmsArray.add(dbmsPrecondition);
    }

    public boolean checkDbmsType(Migrator migrator) {
        boolean returnvalue = false;
        try {
            for (int i = 0; i < dbmsArray.size(); i++) {
                DBMSPrecondition dbmsPrecondition = dbmsArray.get(i);
                //String dbproduct = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();
                if (dbmsPrecondition.checkDatabaseType(migrator)) {
                    returnvalue = true;
                    break;
                }


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return returnvalue;

    }
}
