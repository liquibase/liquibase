package liquibase.util.csv.opencsv.bean;

import liquibase.util.csv.opencsv.CSVReader;

import java.io.IOException;

/**
 * Allows for the mapping of columns with their positions.  Using this strategy requires all the columns
 * to be present in the csv file and for them to be in a particular order.   Also this strategy requires
 * that the file does NOT have an header.  That said the main use of this strategy is files that
 * do not have headers.
 *
 * @param <T> - Type of object that is being processed.
 */

public class ColumnPositionMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {
   private String[] columnMapping = new String[]{};

   /**
    * Default Constructor.
    */
   public ColumnPositionMappingStrategy() {
   }

   /**
    * Captures the header from the reader - required by the MappingStrategy interface and is a do
    * nothing method for the ColumnPositionMappingStrategy.
    *
    * @param reader - CSVReader.
    * @throws IOException - would be thrown by the CSVReader if it was used :)
    */
   public void captureHeader(CSVReader reader) throws IOException {
      //do nothing, first line is not header
   }

   /**
    * gets the column position for a given column name.
    * @param name the column name
    * @return - column position or null if the name does not map.
    */
   @Override
   public Integer getColumnIndex(String name) {
      return indexLookup.get(name);
   }

   /**
    * gets a column name.
    * @param col - position of the column.
    * @return - column name or null if col > number of mappings.
    */
   @Override
   public String getColumnName(int col) {
      return (col < columnMapping.length) ? columnMapping[col] : null;
   }

   /**
    * Retrieves the column mappings.
    * @return - String array with the column mappings.
    */
   public String[] getColumnMapping() {
      return columnMapping.clone();
   }

   /**
    * Setter for the ColumnMappings.
    * @param columnMapping - column names to be mapped.
    */
   public void setColumnMapping(String... columnMapping) {
       this.columnMapping = (columnMapping != null) ? columnMapping.clone() : new String[]{};
      resetIndexMap();
      createIndexLookup(this.columnMapping);
   }
}
