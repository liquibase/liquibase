package liquibase.dbdoc;

import liquibase.GlobalConfiguration;
import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.SortedSet;

public class HTMLListWriter {
    private final Resource outputDir;
    private final String directory;
    private final String filename;
    private final String title;

    public HTMLListWriter(String title, String filename, String subdir, Resource outputDir) {
        this.title = title;
        this.outputDir = outputDir;
        this.filename = filename;
        this.directory = subdir;
    }

    public void writeHTML(SortedSet objects) throws IOException {

        try (Writer fileWriter = new OutputStreamWriter(outputDir.resolve(filename).openOutputStream(new OpenOptions()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())) {
            fileWriter.append("<HTML>\n" + "<HEAD><meta charset=\"utf-8\"/>\n" + "<TITLE>\n");
            fileWriter.append(title);
            fileWriter.append("\n" + "</TITLE>\n" + "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">\n" + "</HEAD>\n" + "<BODY BGCOLOR=\"white\">\n" + "<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">\n" + "<B>");
            fileWriter.append(title);
            fileWriter.append("</B></FONT>\n" + "<BR>\n" + "<TABLE BORDER=\"0\" WIDTH=\"100%\" SUMMARY=\"\">" + "<TR>\n" + "<TD NOWRAP><FONT CLASS=\"FrameItemFont\">");

            String currentSchema = null;
            if (objects.size() > 0 && objects.first().getClass() == Table.class) {
                currentSchema = ((Table )objects.first()).getAttribute("schema", new Schema()).toString();
                fileWriter.append("<div class='schema-name'>" + currentSchema + "</i></b></div>");
            }


            for (Object object : objects) {
                if (object.getClass() == Table.class) {
                    String tableSchema = ((Table) object).getAttribute("schema", new Schema()).toString();
                    if (!tableSchema.equals(currentSchema)) {
                        currentSchema = tableSchema;
                        fileWriter.append("<p>");
                        fileWriter.append("<b><i>" + currentSchema + "</i></b><br>");
                    }
                    fileWriter.append("<A HREF=\"");
                    fileWriter.append(directory + System.getProperty("file.separator") + tableSchema);
                }
                else {
                    fileWriter.append("<A HREF=\"");
                    fileWriter.append(directory);
                }
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
        }
    }

    public String getTargetExtension() {
        return ".html";
    }
}
