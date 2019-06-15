package liquibase.test;

import liquibase.diff.DiffResult;
import liquibase.structure.core.ForeignKey;

import static org.junit.Assert.fail;

/**
 * Utility class to make asserts on diffresults
 * @author lujop
 */
public class DiffResultAssert {
    private DiffResult diff;

    private DiffResultAssert() {        
    }
    
    /**
     * Constructs a DiffResultAssert to make assertions on a diffresult     
     */
    public static DiffResultAssert assertThat(DiffResult diffResult) {
        DiffResultAssert da=new DiffResultAssert();
        da.diff=diffResult;
        return da;
    }

    /**
     * Checks that diffresult contains a foreign key with the given name
     * @param fkName Foreign key name
     */
    public DiffResultAssert containsMissingForeignKeyWithName(String fkName) {
        for(ForeignKey fk:diff.getMissingObjects(ForeignKey.class)) {
            if(fk.getName().equalsIgnoreCase(fkName))
                return this;
        }
        fail("Foreign key with name "+fkName+" not found");
        return this;
    }
}
