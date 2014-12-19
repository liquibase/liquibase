package liquibase.serializer.core.json;

import liquibase.changelog.ChangeSet;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import liquibase.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonChangeLogSerializer extends YamlChangeLogSerializer {

    @Override
    public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("{ \"databaseChangeLog\": [\n");
        int i = 0;
        for (ChangeSet changeSet : changeSets) {
            String serialized = serialize(changeSet, true);
            if (++i < changeSets.size()) {
                serialized = serialized.replaceFirst("}\\s*$", "},\n");
            }
            writer.write(StringUtils.indent(serialized, 2));
            writer.write("\n");
        }
        writer.write("]}");
        writer.flush();
    }

    @Override
    protected Yaml createYaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

        return new Yaml(new LiquibaseRepresenter(), dumperOptions);
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        String out = yaml.dumpAs(toMap(object), Tag.MAP, DumperOptions.FlowStyle.FLOW);
        out = out.replaceAll("!!int \"(\\d+)\"", "$1");
        out = out.replaceAll("!!bool \"(\\w+)\"", "$1");
        out = out.replaceAll("!!timestamp \"([^\"]*)\"", "$1");
        return out;
    }


    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
