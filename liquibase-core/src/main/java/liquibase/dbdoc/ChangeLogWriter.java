package liquibase.dbdoc;

import liquibase.GlobalConfiguration;
import liquibase.resource.Resource;
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

        try (BufferedWriter changeLogStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile,
                false), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))) {
            Resource stylesheet = resourceAccessor.get(physicalFilePath);
            if (stylesheet == null) {
                throw new IOException("Can not find " + changeLog);
            }
            try (InputStream stream = stylesheet.openInputStream()) {
                changeLogStream.write("<html><body><pre>\n");
                changeLogStream.write(StreamUtil.readStreamAsString(stream).replace("<", "&lt;").replace(">", "&gt;"));
                changeLogStream.write("\n</pre></body></html>");
            }
        }
    }
}
