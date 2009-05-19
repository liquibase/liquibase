package liquibase.util.csv.opencsv.bean;

import java.util.HashMap;
import java.util.Map;

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

public class HeaderColumnNameTranslateMappingStrategy extends HeaderColumnNameMappingStrategy {
    private Map columnMapping = new HashMap();
    @Override
    protected String getColumnName(int col) {
        return (String) getColumnMapping().get(header[col]);
    }
    public Map getColumnMapping() {
        return columnMapping;
    }
    public void setColumnMapping(Map columnMapping) {
        this.columnMapping = columnMapping;
    }
}
