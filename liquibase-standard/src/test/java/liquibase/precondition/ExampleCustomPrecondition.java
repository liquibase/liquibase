package liquibase.precondition;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.CustomPreconditionFailedException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExampleCustomPrecondition implements CustomPrecondition {

    private String name;
    private String count;


    @Override
    public void check(Database database) throws CustomPreconditionFailedException {
        Scope.getCurrentScope().getLog(getClass()).info("Custom precondition ran. Name: "+name+", count: "+count  );

//        throw new CustomPreconditionFailedException("custom precondition failed", new RuntimeException());
    }
}
