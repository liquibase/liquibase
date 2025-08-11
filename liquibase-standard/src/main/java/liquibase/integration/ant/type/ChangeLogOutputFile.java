package liquibase.integration.ant.type;

import liquibase.serializer.ChangeLogSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.types.resources.FileResource;

@Getter
@Setter
public class ChangeLogOutputFile {
    private FileResource outputFile;
    private String encoding;
    private ChangeLogSerializer changeLogSerializer;

}
