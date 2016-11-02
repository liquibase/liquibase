package liquibase.sdk.supplier.resource;

import liquibase.change.ChangeFactory;
import liquibase.change.core.CreateProcedureChange;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.HsqlDatabase;
import liquibase.resource.ResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ResourceSupplier {

    private static final liquibase.resource.ResourceAccessor RESOURCE_ACCESSOR = new SimpleResourceAccessor();

    private static final String usersCsv = "username, fullname, pk_id\n" +
            "nvoxland, Nathan Voxland, 1\n" +
            "bob, Bob Bobson, 2";

    private static final String fileSql = "select * from person";

    public ResourceAccessor getSimpleResourceAccessor() {
        return RESOURCE_ACCESSOR;
    }

    private static class SimpleResourceAccessor implements liquibase.resource.ResourceAccessor {

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            InputStream stream = null;
            String encoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
            if (path.toLowerCase().endsWith("csv")) {
                stream = new ByteArrayInputStream(usersCsv.getBytes(encoding));
            } else if (path.toLowerCase().endsWith("my-logic.sql")) {
                stream = new ByteArrayInputStream(((String)ChangeFactory.getInstance().getChangeMetaData(new CreateProcedureChange()).getParameters().get("procedureBody").getExampleValue(new HsqlDatabase())).getBytes(encoding));
            } else if (path.toLowerCase().endsWith("sql")) {
                stream =new ByteArrayInputStream(fileSql.getBytes(encoding));
            } else {
                throw new RuntimeException("Unknown resource type: "+ path);
            }
            return new HashSet<InputStream>(Arrays.asList(stream));
        }

        @Override
        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
            return null;
        }

        @Override
        public ClassLoader toClassLoader() {
            return this.getClass().getClassLoader();
        }
    }
}
