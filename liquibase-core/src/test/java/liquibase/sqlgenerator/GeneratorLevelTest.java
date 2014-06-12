package liquibase.sqlgenerator;

import static org.junit.Assert.*;

import liquibase.actiongenerator.ActionGenerator;
import org.junit.Test;

public class GeneratorLevelTest {

    @SuppressWarnings("unchecked")
	@Test
    public void checkLevelsAndNaming() {
        for (ActionGenerator generator : SqlGeneratorFactory.getInstance().getGenerators()) {
            int specializationlevel = generator.getPriority();
            String className = generator.getClass().getName();
            if (className.contains(".ext.")) {
                //not one to test, a test class
            } else if (className.endsWith("Generator")) {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.PRIORITY_DEFAULT, specializationlevel);
            } else {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.PRIORITY_DATABASE, specializationlevel);
            }
        }
    }
}
