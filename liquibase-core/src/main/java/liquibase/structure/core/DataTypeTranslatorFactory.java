package liquibase.structure.core;

import liquibase.Scope;
import liquibase.servicelocator.AbstractServiceFactory;

public class DataTypeTranslatorFactory extends AbstractServiceFactory<DataTypeTranslator> {

    /**
     * Constructor is protected because it should be used as a singleton.
     */
    protected DataTypeTranslatorFactory(Scope scope) {
        super(scope);
    }

    @Override
    protected Class<DataTypeTranslator> getServiceClass() {
        return DataTypeTranslator.class;
    }

    @Override
    protected int getPriority(DataTypeTranslator obj, Scope scope, Object... args) {
        return obj.getPriority(scope);
    }

    public DataTypeTranslator getTranslator(Scope scope) {
        return this.getService(scope);
    }
}
