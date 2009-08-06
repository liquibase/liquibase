package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableWriter extends HTMLWriter {

    public TableWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "tables"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting table \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
        writeColumns(fileWriter, ((Table) object), database);
    }

    private void writeColumns(FileWriter fileWriter, Table table, Database database) throws IOException {
        List<List<String>> cells = new ArrayList<List<String>>();

        for (Column column : table.getColumns()) {
            cells.add(Arrays.asList(column.getDataTypeString(database),
                    "<A HREF=\"../columns/" + table.getName() + "." + column.getName() + ".html" + "\">" + column.getName() + "</A>"));
            //todo: add foreign key info to columns?
        }


        writeTable("Current Columns", cells, fileWriter);

    }
}
