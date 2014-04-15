package liquibase.change.core.supplier;

import liquibase.change.AddColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AddColumnConfigSupplier {

    public Collection<AddColumnConfig> getStandardPermutations(Database database) throws Exception {

        List<AddColumnConfig> columnConfigs = new ArrayList<AddColumnConfig>();

        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int"));

        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("varchar(255)").setDefaultValue("car"));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setDefaultValueNumeric(712));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("float").setDefaultValueNumeric(3812.112));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("boolean").setDefaultValueBoolean(false));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("boolean").setDefaultValueBoolean(true));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("datetime").setDefaultValueComputed(new DatabaseFunction("NOW()")));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("datetime").setDefaultValueDate(new Date(737138163)));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("datetime").setDefaultValueDate("2013-02-13T13:44:03"));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("datetime").setDefaultValueSequenceNext(new SequenceNextValueFunction("seq_test")));

        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)).setDefaultValueNumeric(-1));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)).setAutoIncrement(true));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)).setAutoIncrement(true).setStartWith(BigInteger.valueOf(3)));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)).setAutoIncrement(true).setIncrementBy(BigInteger.valueOf(4)));
        columnConfigs.add((AddColumnConfig) new AddColumnConfig().setName("test_col").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)).setAutoIncrement(true).setStartWith(BigInteger.valueOf(3)).setIncrementBy(BigInteger.valueOf(4)));

        return columnConfigs;
    }

    protected Collection<? extends String> getTestValues(String param, Database database) {
        throw new UnexpectedLiquibaseSdkException("Unexpected param: "+param);
    }

}
