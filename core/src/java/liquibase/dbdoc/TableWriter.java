package liquibase.dbdoc;

import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.change.Change;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableWriter extends HTMLWriter {

    public TableWriter(File rootOutputDir) {
        super(new File(rootOutputDir, "tables"));
    }

    protected String createTitle(Object object) {
        return object.toString() + " (Table)";
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes) throws IOException {
        writeColumns(fileWriter, ((Table) object));
    }

    private void writeColumns(FileWriter fileWriter, Table table) throws IOException {
        List<List<String>> cells = new ArrayList<List<String>>();

        for (Column column : table.getColumns()) {
            cells.add(Arrays.asList(column.getTypeName(),
                    "<A HREF=\"../columns/" + table.getName() + "." + column.getName() + ".html" + "\">" + column.getName() + "</A>"));
            //todo: add foreign key info to columns?
        }


        writeTable("Current Columns", cells, fileWriter);

    }
}
