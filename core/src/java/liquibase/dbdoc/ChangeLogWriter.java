package liquibase.dbdoc;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChangeLogWriter {
    protected File outputDir;
    private FileOpener fileOpener;

    public ChangeLogWriter(FileOpener fileOpener, File rootOutputDir) {
        this.outputDir = new File(rootOutputDir, "changelogs");
        this.fileOpener = fileOpener;
    }

    public void writeChangeLog(DatabaseChangeLog changeLog) throws IOException {
        InputStream stylesheet = fileOpener.getResourceAsStream(changeLog.getPhysicalFilePath());
        if (stylesheet == null) {
            throw new IOException("Can not find "+changeLog.getFilePath());
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


        File xmlFile = new File(outputDir, changeLog.getFilePath() + ".xml");
        xmlFile.getParentFile().mkdirs();

        FileOutputStream changeLogStream = new FileOutputStream(xmlFile, false);
        try {
            StreamUtil.copy(stylesheet, changeLogStream);
        } finally {
            changeLogStream.close();
        }

    }


}
