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
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CsvToBean {

    public CsvToBean() {
    }

    public List parse(MappingStrategy mapper, Reader reader) {
        try {
            CSVReader csv = new CSVReader(reader);
            mapper.captureHeader(csv);
            String[] line;
            List list = new ArrayList();
            while(null != (line = csv.readNext())) {
                Object obj = processLine(mapper, line);
                list.add(obj); 
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV!", e);
        }
    }

    protected Object processLine(MappingStrategy mapper, String[] line) throws IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
        Object bean = mapper.createBean();
        for(int col = 0; col < line.length; col++) {
            String value = line[col];
            PropertyDescriptor prop = mapper.findDescriptor(col);
            if (null != prop) {
                Object obj = convertValue(value, prop);
                prop.getWriteMethod().invoke(bean, new Object[] {obj});
            }
        }
        return bean;
    }

    protected Object convertValue(String value, PropertyDescriptor prop) throws InstantiationException, IllegalAccessException {
        PropertyEditor editor = getPropertyEditor(prop);
        Object obj = value;
        if (null != editor) {
            editor.setAsText(value);
            obj = editor.getValue();
        }
        return obj;
    }

    /*
     * Attempt to find custom property editor on descriptor first, else try the propery editor manager.
     */
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        Class cls = desc.getPropertyEditorClass();
        if (null != cls) return (PropertyEditor) cls.newInstance();
        return PropertyEditorManager.findEditor(desc.getPropertyType());
    }

}
