package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UniqueConstraintComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        List<String> hashes = new ArrayList<>();
        if (databaseObject.getName() != null) {
            hashes.add(databaseObject.getName().toLowerCase());
        }

        Relation table = ((UniqueConstraint) databaseObject).getRelation();
        if (table != null) {
            hashes.addAll(Arrays.asList(DatabaseObjectComparatorFactory.getInstance().hash(table, chain.getSchemaComparisons(), accordingTo)));
        }

        return hashes.toArray(new String[hashes.size()]);
    }


    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!((databaseObject1 instanceof UniqueConstraint) && (databaseObject2 instanceof UniqueConstraint))) {
            return false;
        }

        UniqueConstraint thisConstraint = (UniqueConstraint) databaseObject1;
        UniqueConstraint otherConstraint = (UniqueConstraint) databaseObject2;

        int thisConstraintSize = thisConstraint.getColumns().size();
        int otherConstraintSize = otherConstraint.getColumns().size();

        if ((thisConstraint.getRelation() != null) && (otherConstraint.getRelation() != null)) {
            if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisConstraint.getRelation(), otherConstraint.getRelation(), chain.getSchemaComparisons(), accordingTo)) {
                return false;
            }
            if ((databaseObject1.getSchema() != null) && (databaseObject2.getSchema() != null) &&
                !DatabaseObjectComparatorFactory.getInstance().isSameObject(databaseObject1.getSchema(),
                    databaseObject2.getSchema(), chain.getSchemaComparisons(), accordingTo)) {
                return false;
            }

            if ((databaseObject1.getName() != null) && (databaseObject2.getName() != null) &&
                DefaultDatabaseObjectComparator.nameMatches(databaseObject1, databaseObject2, accordingTo)) {
                return true;
            } else {
                if ((thisConstraintSize == 0) || (otherConstraintSize == 0)) {
                    return DefaultDatabaseObjectComparator.nameMatches(databaseObject1, databaseObject2, accordingTo);
                }

                if ((thisConstraintSize > 0) && (otherConstraintSize > 0) && (thisConstraintSize != otherConstraintSize)) {
                    return false;
                }

                for (int i = 0; i < otherConstraintSize; i++) {
                    if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisConstraint.getColumns().get(i).setRelation(thisConstraint.getRelation()), otherConstraint.getColumns().get(i).setRelation(otherConstraint.getRelation()), chain.getSchemaComparisons(), accordingTo)) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            if ((thisConstraintSize > 0) && (otherConstraintSize > 0) && (thisConstraintSize != otherConstraintSize)) {
                return false;
            }

            if (!DefaultDatabaseObjectComparator.nameMatches(databaseObject1, databaseObject2, accordingTo)) {
                return false;
            }

            if ((databaseObject1.getSchema() != null) && (databaseObject2.getSchema() != null)) {
                return DatabaseObjectComparatorFactory.getInstance().isSameObject(databaseObject1.getSchema(), databaseObject2.getSchema(), chain.getSchemaComparisons(), accordingTo);
            } else {
                return true;
            }
        }

    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("columns");
        exclude.add("backingIndex");
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("columns", databaseObject1, databaseObject2, new ObjectDifferences.CompareFunction() {
            @Override
            public boolean areEqual(Object referenceValue, Object compareToValue) {
                List<Column> referenceList = (List) referenceValue;
                List<Column> compareList = (List) compareToValue;

                if (referenceList.size() != compareList.size()) {
                    return false;
                }
                for (int i=0; i<referenceList.size(); i++) {
                    if (!StringUtil.trimToEmpty((referenceList.get(i)).getName()).equalsIgnoreCase(StringUtil.trimToEmpty(compareList.get(i).getName()))) {
                        return false;
                    }
                }
                return true;
            }
        });

        differences.compare("backingIndex", databaseObject1, databaseObject2, new ObjectDifferences.StandardCompareFunction(chain.getSchemaComparisons(), accordingTo));
        return differences;
    }
}
