package liquibase.dbdoc;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChangeLogWriter {
    protected File outputDir;
    private ResourceAccessor resourceAccessor;

    public ChangeLogWriter(ResourceAccessor resourceAccessor, File rootOutputDir) {
        this.outputDir = new File(rootOutputDir, "changelogs");
        this.resourceAccessor = resourceAccessor;
    }

    public void writeChangeLog(String changeLog, String physicalFilePath) throws IOException {
        InputStream stylesheet = resourceAccessor.getResourceAsStream(physicalFilePath);
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


        File xmlFile = new File(outputDir, changeLog + ".xml");
        xmlFile.getParentFile().mkdirs();

        FileOutputStream changeLogStream = new FileOutputStream(xmlFile, false);
        try {
            StreamUtil.copy(stylesheet, changeLogStream);
        } finally {
            changeLogStream.close();
        }

    }


}
