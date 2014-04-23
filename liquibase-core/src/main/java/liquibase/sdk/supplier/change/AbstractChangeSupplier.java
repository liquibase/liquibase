package liquibase.sdk.supplier.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.CollectionUtil;

import java.util.*;

public abstract class AbstractChangeSupplier<T extends Change> implements ChangeSupplier<T> {

    private final String changeName;

    protected AbstractChangeSupplier(Class<? extends Change> changeClass) {
        try {
            changeName = changeClass.newInstance().getSerializedObjectName();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public String getChangeName() {
           return changeName;
    }

    @Override
    public Change[] revertDatabase(T change) throws Exception {
        return null;
    }

    public Collection<Change> getAllParameterPermutations(Database database) throws Exception {
        ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(getChangeName());
        Set<Set<String>> parameterSets = CollectionUtil.powerSet(changeMetaData.getParameters().keySet());

        List<Change> changes = new ArrayList<Change>();
        for (Set<String> params : parameterSets) {
            Change change = ChangeFactory.getInstance().create(getChangeName());
            for (String param : params) {
                ChangeParameterMetaData changeParam = changeMetaData.getParameters().get(param);
                Object exampleValue = changeParam.getExampleValue(database);
                changeParam.setValue(change, exampleValue);
            }
            changes.add(change);
        }

        return changes;
    }
}
