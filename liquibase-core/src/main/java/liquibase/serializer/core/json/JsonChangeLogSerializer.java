package liquibase.serializer.core.json;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

public class JsonChangeLogSerializer extends YamlChangeLogSerializer {

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
        return yaml.dumpAs(toMap(object), Tag.MAP, DumperOptions.FlowStyle.FLOW);
    }


    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
