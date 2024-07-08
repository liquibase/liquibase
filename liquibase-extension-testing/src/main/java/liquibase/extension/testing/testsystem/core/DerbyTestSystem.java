package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import org.jetbrains.annotations.NotNull;

public class DerbyTestSystem extends DatabaseTestSystem {

     public DerbyTestSystem() {
        super("derby");
    }

    public DerbyTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() throws Exception{
         throw new IllegalArgumentException("Cannot create container for derby. Use URL");
    }

    @Override
    protected String[] getSetupSql() {
        return new String[] {
                "create schema "+getAltSchema(),
        };
    }

}
