package liquibase.precondition;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.CustomPreconditionFailedException;
import lombok.Getter;

@Getter
public class ExampleCustomPrecondition implements CustomPrecondition {

    private String name;
    private String count;


    public void setName(String name) {
        this.name = name;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public void check(Database database) throws CustomPreconditionFailedException {
        Scope.getCurrentScope().getLog(getClass()).info("Custom precondition ran. Name: "+name+", count: "+count  );

//        throw new CustomPreconditionFailedException("custom precondition failed", new RuntimeException());
    }
}
