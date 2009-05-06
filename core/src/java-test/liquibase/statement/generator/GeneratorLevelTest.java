package liquibase.statement.generator;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GeneratorLevelTest {

    @SuppressWarnings("unchecked")
	@Test
    public void checkLevelsAndNaming() {
        for (SqlGenerator generator : SqlGeneratorFactory.getInstance().getGenerators()) {
            int specializationlevel = generator.getSpecializationLevel();
            String className = generator.getClass().getName();
            if (className.endsWith("Generator")) {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.SPECIALIZATION_LEVEL_DEFAULT, specializationlevel);
            } else {
                assertEquals("Incorrect level/naming convention for "+ className, SqlGenerator.SPECIALIZATION_LEVEL_DATABASE_SPECIFIC, specializationlevel);
            }
        }
    }
}
