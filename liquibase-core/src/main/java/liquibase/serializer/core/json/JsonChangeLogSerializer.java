package liquibase.serializer.core.json;

import liquibase.changelog.ChangeLogChild;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import liquibase.util.StringUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonChangeLogSerializer extends YamlChangeLogSerializer {

    @Override
    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
        writer.write("{ \"databaseChangeLog\": [\n");
        int i = 0;
        for (T child : children) {
            String serialized = serialize(child, true);
            if (++i < children.size()) {
                serialized = serialized.replaceFirst("}\\s*$", "},\n");
            }
            writer.write(StringUtil.indent(serialized, 2));
            writer.write("\n");
        }
        writer.write("]}");
        writer.flush();
    }

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
