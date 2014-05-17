package liquibase.sdk.supplier.change;

import liquibase.change.*;
import liquibase.change.core.supplier.AddColumnConfigSupplier;
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

    @Override
    public Collection<Change> getAllParameterPermutations(Database database) throws Exception {
        ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(getChangeName());
        Set<Set<String>> parameterSets = CollectionUtil.powerSet(changeMetaData.getParameters().keySet());

        List<Change> changes = new ArrayList<Change>();
        for (Collection<String> params : parameterSets) {
            Map<String, List<Object>> parameterValues = new HashMap<String, List<Object>>();
            for (String param : params) {
                ChangeParameterMetaData changeParam = changeMetaData.getParameters().get(param);
                parameterValues.put(param, new ArrayList());
                parameterValues.get(param).addAll(getTestValues(changeParam, database));
            }

            for (Map<String, ?> valuePermutation : CollectionUtil.permutations(parameterValues)) {
                Change change = ChangeFactory.getInstance().create(getChangeName());
                for (Map.Entry<String, ?> entry : valuePermutation.entrySet()) {
                    ChangeParameterMetaData changeParam = changeMetaData.getParameters().get(entry.getKey());
                    changeParam.setValue(change, entry.getValue());
                }
                changes.add(change);
            }
        }

        return changes;
    }

    protected List getTestValues(ChangeParameterMetaData changeParam, Database database) throws Exception {
        List values = new ArrayList();

        if (changeParam.getDataType().equals("list of addColumnConfig")) {
            for (AddColumnConfig config : getAddColumnConfigSupplier().getStandardPermutations(database)) {
                values.add(new ArrayList<ColumnConfig>(Arrays.asList(config)));
            }

        } else {
            Object exampleValue = changeParam.getExampleValue(database);
            values.add(exampleValue);
        }
        return values;
    }

    protected AddColumnConfigSupplier getAddColumnConfigSupplier() {
        return new AddColumnConfigSupplier();
    }

    @Override
    public boolean isValid(Change change, Database database) {
        return !change.validate(database).hasErrors();
    }
}
