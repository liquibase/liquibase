package liquibase;

import liquibase.database.Database;
import lombok.Getter;

@Getter
public class RuntimeEnvironment {
    private final Database targetDatabase;
    private final Contexts contexts;
    private final LabelExpression labels;

    public RuntimeEnvironment(Database targetDatabase, Contexts contexts, LabelExpression labelExpression) {
        this.targetDatabase = targetDatabase;
        this.contexts = contexts;
        this.labels = labelExpression;
    }

}
