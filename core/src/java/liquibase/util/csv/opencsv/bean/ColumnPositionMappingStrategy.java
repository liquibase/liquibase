package liquibase.util.csv.opencsv.bean;

import liquibase.util.csv.CSVReader;

import java.io.IOException;

/**
 Copyright 2007 Kyle Miller.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class ColumnPositionMappingStrategy extends HeaderColumnNameMappingStrategy {
    protected String[] columnMapping = new String[] {};
    public void captureHeader(CSVReader reader) throws IOException {
        //do nothing, first line is not header
    }
    @Override
    protected String getColumnName(int col) {
        return (null != columnMapping && col < columnMapping.length) ? columnMapping[col] : null ;
    }
    public String[] getColumnMapping() {
        return columnMapping;
    }
    public void setColumnMapping(String[] columnMapping) {
        this.columnMapping = columnMapping;
    }
}
