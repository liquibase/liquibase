package liquibase.dbdoc;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChangeLogWriter {
    protected Path outputDir;
    private ResourceAccessor resourceAccessor;
    
    public ChangeLogWriter(ResourceAccessor resourceAccessor, Path rootOutputDir) {
        this.outputDir = rootOutputDir.resolve("changelogs");
        this.resourceAccessor = resourceAccessor;
    }
    
    public void writeChangeLog(String changeLog, String physicalFilePath) throws IOException {
        String changeLogOutFile = changeLog.replace(":", "_");
        Path xmlFile = outputDir.resolve(changeLogOutFile.toLowerCase() + ".html");
        xmlFile.getParent().toFile().mkdirs();
        
        BufferedWriter changeLogStream = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(xmlFile), LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
        try (InputStream stylesheet = resourceAccessor.openStream(null, physicalFilePath)) {
            if (stylesheet == null) {
                throw new IOException("Can not find " + changeLog);
            }
            changeLogStream.write("<html><body><pre>\n");
            changeLogStream.write(StreamUtil.readStreamAsString(stylesheet).replace("<", "&lt;").replace(">", "&gt;"));
            changeLogStream.write("\n</pre></body></html>");
        } finally {
            changeLogStream.close();
        }
    }
}
