package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

import java.util.ArrayList;
import java.util.List;

public class AllDatabaseTypes extends ParameterSupplier {
    @Override
    public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        List<PotentialAssignment> returnList = new ArrayList<PotentialAssignment>();
        for (Database database :  DatabaseFactory.getInstance().getImplementedDatabases()) {
            returnList.add(PotentialAssignment.forValue(database.getShortName(), database));
        }

        return returnList;
    }
}
