package liquibase.test;

import java.util.Optional;
import java.util.function.Predicate;
import liquibase.diff.DiffResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;

import static org.junit.Assert.fail;

/**
 * Utility class to make asserts on diffresults
 * @author lujop
 */
public class DiffResultAssert {
    private DiffResult diff;

    private DiffResultAssert(DiffResult diff) {
        this.diff = diff;
    }

    /**
     * Constructs a DiffResultAssert to make assertions on a diffresult
     */
    public static DiffResultAssert assertThat(DiffResult diffResult) {
        return new DiffResultAssert(diffResult);
    }

    /**
     * Checks that diffresult contains a foreign key with the given name
     * @param fkName Foreign key name
     */
    public DiffResultAssert containsMissingForeignKeyWithName(String fkName) {
        return checkContainsMissingObject(ForeignKey.class, foreignKey -> foreignKey.getName().equalsIgnoreCase(fkName), "Foreign key with name "+fkName+" not found");
    }

    public <T extends DatabaseObject> DiffResultAssert containsMissingObject(Class<T> type, Predicate<T> condition) {
        return checkContainsMissingObject(type, condition, type.getSimpleName() + " satisfying condition not found");
    }

    public DiffResultAssert containsMissingObject(DatabaseObject object) {
        return checkContainsMissingObject(object.getClass(), object::equals, object + " not found");
    }

    private <T extends DatabaseObject> DiffResultAssert checkContainsMissingObject(Class<T> type, Predicate<T> condition, String failMessage) {
        Optional<? extends DatabaseObject> first = diff.getMissingObjects(type).stream()
            .filter(condition)
            .findFirst();
        if (!first.isPresent()) {
            fail(failMessage);
        }
        return this;
    }
}
