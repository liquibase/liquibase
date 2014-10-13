package liquibase.dbdoc;

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
        InputStream stylesheet = StreamUtil.singleInputStream(physicalFilePath, resourceAccessor);
        if (stylesheet == null) {
            throw new IOException("Can not find "+changeLog);
        }

//        File file = outputDir;
//        String[] splitPath = (changeLog.getFilePath() + ".xml").split("/");
//        for (int i =0; i < splitPath.length; i++) {
//            String pathPart = splitPath[i];
//            file = new File(file, pathPart);
//            if (i < splitPath.length - 1) {
//                file.mkdirs();
//            }
//        }


        String changeLogOutFile = changeLog.replace(":", "_");
        File xmlFile = new File(outputDir, changeLogOutFile + ".html");
        xmlFile.getParentFile().mkdirs();

        BufferedWriter changeLogStream = new BufferedWriter(new FileWriter(xmlFile, false));
        try {
            changeLogStream.write("<html><body><pre>\n");
            changeLogStream.write(StreamUtil.getStreamContents(stylesheet).replace("<", "&lt;").replace(">", "&gt;"));
            changeLogStream.write("\n</pre></body></html>");
        } finally {
            changeLogStream.close();
        }

    }


}
