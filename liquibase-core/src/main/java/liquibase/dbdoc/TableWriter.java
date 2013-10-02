package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

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
    	final Table table = (Table) object;
    	writeTableRemarks(fileWriter, table, database);
		writeColumns(fileWriter, table, database);
    }

    private void writeColumns(FileWriter fileWriter, Table table, Database database) throws IOException {
        List<List<String>> cells = new ArrayList<List<String>>();

        for (Column column : table.getColumns()) {
            String remarks = column.getRemarks();
            cells.add(Arrays.asList(column.getType().toString(),
                    "<A HREF=\"../columns/" + table.getName().toLowerCase() + "." + column.getName().toLowerCase() + ".html" + "\">" + column.getName() + "</A>",
                    remarks != null ? remarks : ""));
            //todo: add foreign key info to columns?
        }


        writeTable("Current Columns", cells, fileWriter);
    }
    
    private void writeTableRemarks(FileWriter fileWriter, Table table, Database database) throws IOException {
        final String tableRemarks = table.getRemarks();
        if (tableRemarks != null && tableRemarks.length() > 0) {
        	final List<List<String>> cells = new ArrayList<List<String>>();
        	cells.add(Arrays.asList(tableRemarks));
        	writeTable("Table Description", cells, fileWriter);
        }
    }
}
