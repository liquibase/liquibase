package liquibase.util.csv.opencsv.bean;


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

import liquibase.util.csv.opencsv.CSVReader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;

public interface MappingStrategy {

    /**
     * Implementation will have to return a property descriptor from a bean based on the current column.
     */
    public abstract PropertyDescriptor findDescriptor(int col) throws IntrospectionException;

    public abstract Object createBean() throws InstantiationException, IllegalAccessException;

    /**
     * Implemention of this method can grab the header line before parsing begins to use to map columns
     * to bean properties.
     */
    public void captureHeader(CSVReader reader) throws IOException;

}