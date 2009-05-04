package liquibase.database.statement.generator;

import org.junit.Test;
import static org.junit.Assert.*;

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
