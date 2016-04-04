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

/**
 * The interface for the classes that handle translating between the columns in the csv file
 * to an actual object.
 *
 * @param <T> type of object you are converting the data to.
 */
public interface MappingStrategy<T> {

   /**
    * Implementation will have to return a property descriptor from a bean based on the current column.
    *
    * @param col the column to find the description for
    * @return the related PropertyDescriptor
    * @throws IntrospectionException - thrown on error loading the property descriptors.
    */
   PropertyDescriptor findDescriptor(int col) throws IntrospectionException;

   /**
    * Implementation will have to return - based on the current column - a BeanField containing
    * the {@link java.lang.reflect.Field} and a boolean representing whether the field is required (mandatory) or not.
    *
    * @param col the column to find the field for
    * @return BeanField containing Field and whether it is required
    */
   BeanField findField(int col);

    /**
     * Implementation will return a bean of the type of object you are mapping.
     *
     * @return A new instance of the class being mapped.
     * @throws InstantiationException - thrown on error creating object.
     * @throws IllegalAccessException - thrown on error creating object.
     */
    T createBean() throws InstantiationException, IllegalAccessException;

   /**
    * Implementation of this method can grab the header line before parsing begins to use to map columns
    * to bean properties.
    *
    * @param reader the CSVReader to use for header parsing
    * @throws java.io.IOException if parsing fails
    */
   void captureHeader(CSVReader reader) throws IOException;

   /**
    * Gets the column index that corresponds to a specific colum name.
    * If the CSV file doesn't have a header row, this method will always return
    * null.
    *
    * @param name the column name
    * @return the column index, or null if the name doesn't exist
    */
   Integer getColumnIndex(String name);

   /**
    * Determines whether the mapping strategy is driven by {@link liquibase.util.csv.opencsv.bean.CsvBind} annotations.
    *
    * @return whether the mapping strategy is driven by annotations
    */
   boolean isAnnotationDriven();
}