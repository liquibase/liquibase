package liquibase.sqlgenerator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneratorLevelTest {

    @SuppressWarnings("unchecked")
	@Test
    public void checkLevelsAndNaming() {
        for (SqlGenerator generator : SqlGeneratorFactory.getInstance().getGenerators()) {
            int specializationlevel = generator.getPriority();
            String className = generator.getClass().getName();
            if (className.contains(".ext.")) {
                //not one to test, a test class
            } else if (className.endsWith("CreateTableGeneratorInformix")) {
                //had to change level for some reason
            } else if (className.endsWith("Generator")) {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.PRIORITY_DEFAULT, specializationlevel);
            } else {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.PRIORITY_DATABASE, specializationlevel);
            }
        }
    }
}
