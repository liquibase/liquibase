package liquibase.dbdoc;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.*;

public class ChangeLogWriter {
    protected File outputDir;
    private ResourceAccessor resourceAccessor;
    
    public ChangeLogWriter(ResourceAccessor resourceAccessor, File rootOutputDir) {
        this.outputDir = new File(rootOutputDir, "changelogs");
        this.resourceAccessor = resourceAccessor;
    }
    
    public void writeChangeLog(String changeLog, String physicalFilePath) throws IOException {
        String changeLogOutFile = changeLog.replace(":", "_");
        File xmlFile = new File(outputDir, changeLogOutFile.toLowerCase() + ".html");
        xmlFile.getParentFile().mkdirs();
        
        BufferedWriter changeLogStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile,
        false), LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
        try (InputStream stylesheet = StreamUtil.singleInputStream(physicalFilePath, resourceAccessor);) {
            if (stylesheet == null) {
                throw new IOException("Can not find " + changeLog);
            }
            changeLogStream.write("<html><body><pre>\n");
            changeLogStream.write(StreamUtil.getStreamContents(stylesheet).replace("<", "&lt;").replace(">", "&gt;"));
            changeLogStream.write("\n</pre></body></html>");
        } finally {
            changeLogStream.close();
        }
    }
}
