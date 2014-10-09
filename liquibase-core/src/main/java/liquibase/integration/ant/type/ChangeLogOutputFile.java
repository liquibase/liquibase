package liquibase.integration.ant.type;

import liquibase.serializer.ChangeLogSerializer;
import org.apache.tools.ant.types.resources.FileResource;

public class ChangeLogOutputFile {
    private FileResource outputFile;
    private String encoding;
    private ChangeLogSerializer changeLogSerializer;

    public ChangeLogSerializer getChangeLogSerializer() {
        return changeLogSerializer;
    }

    public void setChangeLogSerializer(ChangeLogSerializer changeLogSerializer) {
        this.changeLogSerializer = changeLogSerializer;
    }

    public FileResource getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(FileResource outputFile) {
        this.outputFile = outputFile;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
