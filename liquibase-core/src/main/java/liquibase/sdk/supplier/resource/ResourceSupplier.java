package liquibase.sdk.supplier.resource;

import liquibase.Scope;
import liquibase.change.ChangeFactory;
import liquibase.change.core.CreateProcedureChange;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.HsqlDatabase;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.InputStreamSupplier;
import liquibase.resource.ResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

public class ResourceSupplier {

    private static final liquibase.resource.ResourceAccessor RESOURCE_ACCESSOR = new SimpleResourceAccessor();

    private static final String USERS_CSV = "username, fullname, pk_id\n" +
            "nvoxland, Nathan Voxland, 1\n" +
            "bob, Bob Bobson, 2";

    private static final String EXAMPLE_SQL_COMMAND = "select * from person";

    public ResourceAccessor getSimpleResourceAccessor() {
        return RESOURCE_ACCESSOR;
    }

    private static class SimpleResourceAccessor extends AbstractResourceAccessor{

        @Override
        public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
            InputStreamSupplier stream = null;
            String encoding = LiquibaseConfiguration.getInstance().getConfiguration(
                    GlobalConfiguration.class).getOutputEncoding();
            if (streamPath.toLowerCase().endsWith("csv")) {
                stream = () -> new ByteArrayInputStream(USERS_CSV.getBytes(encoding));
            } else if (streamPath.toLowerCase().endsWith("my-logic.sql")) {
                stream = () -> new ByteArrayInputStream(((String) Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(
                        new CreateProcedureChange()).getParameters().get("procedureBody").getExampleValue(
                        new HsqlDatabase())).getBytes(encoding)
                );
            } else if (streamPath.toLowerCase().endsWith("sql")) {
                stream = () -> new ByteArrayInputStream(EXAMPLE_SQL_COMMAND.getBytes(encoding));
            } else {
                throw new RuntimeException("Unknown resource type: "+ streamPath);
            }
            InputStreamList list = new InputStreamList();
            list.add(null, stream);
            return list;
        }

        @Override
        public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
            return null;
        }
    }
}
