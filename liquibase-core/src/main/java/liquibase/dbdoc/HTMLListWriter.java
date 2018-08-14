package liquibase.dbdoc;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.SortedSet;

public class HTMLListWriter {
    private File outputDir;
    private String directory;
    private String filename;
    private String title;

    public HTMLListWriter(String title, String filename, String subdir, File outputDir) {
        this.title = title;
        this.outputDir = outputDir;
        this.filename = filename;
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        this.directory = subdir;
    }

    public void writeHTML(SortedSet objects) throws IOException {
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(new File(outputDir, filename)), LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());

        try {
            fileWriter.append("<HTML>\n" + "<HEAD><meta charset=\"utf-8\"/>\n" + "<TITLE>\n");
            fileWriter.append(title);
            fileWriter.append("\n" + "</TITLE>\n" + "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">\n" + "</HEAD>\n" + "<BODY BGCOLOR=\"white\">\n" + "<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">\n" + "<B>");
            fileWriter.append(title);
            fileWriter.append("</B></FONT>\n" + "<BR>\n" + "<TABLE BORDER=\"0\" WIDTH=\"100%\" SUMMARY=\"\">" + "<TR>\n" + "<TD NOWRAP><FONT CLASS=\"FrameItemFont\">");


            for (Object object : objects) {
                fileWriter.append("<A HREF=\"");
                fileWriter.append(directory);
                fileWriter.append("/");
                fileWriter.append(DBDocUtil.toFileName(object.toString().endsWith(".xml") ? object.toString() : object.toString().toLowerCase()));
                fileWriter.append(getTargetExtension());
                fileWriter.append("\" target=\"objectFrame\">");
                fileWriter.append(StringUtil.escapeHtml(object.toString()));
                fileWriter.append("</A><BR>\n");
            }

            fileWriter.append("</FONT></TD>\n" +
                    "</TR>\n" +
                    "</TABLE>\n" +
                    "\n" +
                    "</BODY>\n" +
                    "</HTML>");
        } finally {
            fileWriter.close();
        }
    }

    public String getTargetExtension() {
        return ".html";
    }
}
