package liquibase.util.csv.opencsv.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2007,2010 Kyle Miller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Expands on HeaderColumnNameMappingStrategy by allowing the user to pass in a map of column names to
 * bean names.  This way the fields in the bean do not have to match the fields in the csv file.
 *
 * @param <T> - class to be mapped.
 */
public class HeaderColumnNameTranslateMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {
   private Map<String, String> columnMapping = new HashMap<>();

   /**
    * Default constructor.
    */
   public HeaderColumnNameTranslateMappingStrategy() {
   }

   /**
    * Retrieves the column name for a given column position
    *
    * @param col - column position.
    * @return - The column name.
    */
   @Override
   public String getColumnName(int col) {
      return (col < header.length) ? columnMapping.get(header[col].toUpperCase()) : null;
   }

   /**
    * retrieves the column mappings of the strategy.
    * @return - the column mappings of the strategy.
    */
   public Map<String, String> getColumnMapping() {
      return columnMapping;
   }

   /**
    * Sets the column mapping to those passed in.
    * @param columnMapping - source column mapping.
    */
   public void setColumnMapping(Map<String, String> columnMapping) {
      this.columnMapping.clear();
      for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
         this.columnMapping.put(entry.getKey().toUpperCase(), entry.getValue());
      }
   }
}
