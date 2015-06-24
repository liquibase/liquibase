package liquibase.dbdoc;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.*;

public class ChangeLogWriter {
    protected File outputDir;
    private ResourceAccessor resourceAccessor;
    private String outputFileEncoding;

    public ChangeLogWriter(ResourceAccessor resourceAccessor, File rootOutputDir, String outputFileEncoding) {
        this.outputDir = new File(rootOutputDir, "changelogs");
        this.resourceAccessor = resourceAccessor;
        this.outputFileEncoding = outputFileEncoding;
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

        Writer changeLogStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), outputFileEncoding));
        try {
            changeLogStream.write("<html>\n");
            changeLogStream.append("<head>");
            changeLogStream.append("<META http-equiv=\"Content-Type\" content=\"text/html; charset=").append(outputFileEncoding).append("\">");
            changeLogStream.append("</head>\n");
            changeLogStream.append("<body><pre>\n");
            changeLogStream.write(StreamUtil.getStreamContents(stylesheet).replace("<", "&lt;").replace(">", "&gt;"));
            changeLogStream.write("\n</pre></body></html>");
        } finally {
            changeLogStream.close();
        }

    }


}
