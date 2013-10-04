package liquibase.util.csv.opencsv.bean;

import liquibase.util.csv.opencsv.CSVReader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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

public class HeaderColumnNameMappingStrategy implements MappingStrategy {
    protected String[] header;
    protected PropertyDescriptor[] descriptors;
    protected Class type;

    @Override
    public void captureHeader(CSVReader reader) throws IOException {
        header = reader.readNext();
    }

    @Override
    public PropertyDescriptor findDescriptor(int col) throws IntrospectionException {
        String columnName = getColumnName(col);
        return (null != columnName && columnName.trim().length()>0) ? findDescriptor(columnName) : null;
    }

    protected String getColumnName(int col) {
        return (null != header && col < header.length) ? header[col] : null;
    }
    protected PropertyDescriptor findDescriptor(String name) throws IntrospectionException {
        if (null == descriptors) descriptors = loadDescriptors(getType()); //lazy load descriptors
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor desc = descriptors[i];
            if (matches(name, desc)) return desc; 
        }
        return null;
    }
    protected boolean matches(String name, PropertyDescriptor desc) {
        return desc.getName().equals(name);
    }
    protected PropertyDescriptor[] loadDescriptors(Class cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        return beanInfo.getPropertyDescriptors();
    }
    @Override
    public Object createBean() throws InstantiationException, IllegalAccessException {
        return type.newInstance();
    }
    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
