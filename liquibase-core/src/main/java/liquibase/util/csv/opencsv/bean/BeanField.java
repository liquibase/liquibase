package liquibase.util.csv.opencsv.bean;

import liquibase.util.StringUtils;

import java.lang.reflect.Field;

/**
 * Used to extend the Field class to add a required flag.  This flag determines if the field has to have information,
 * or in the case of the String class cannot be an empty String.
 */
public class BeanField {
   private final Field field;
   private final boolean required;

   /**
    * @param field    - A java.lang.reflect.Field object.
    * @param required - true if the field is required to contain a value, false if it is allowed to be null or blank String.
    */
   public BeanField(Field field, boolean required) {
      this.field = field;
      this.required = required;
   }

   /**
    * @return - a field object
    * @see java.lang.reflect.Field
    */
   public Field getField() {
      return this.field;
   }

   /**
    *
    * @return - true if the field is required to be set (cannot be null or empty string), false otherwise.
    */
   public boolean isRequired() {
      return this.required;
   }

   /**
    *
    * @param bean - Object containing the field to be set.
    * @param value - String containing the value to set the field to.
    * @param <T> - Type of the bean.
    * @throws IllegalAccessException - Thrown on reflection error.
    */
   public <T> void setFieldValue(T bean, String value) throws IllegalAccessException {
      if (required && (StringUtils.trimToNull(value) == null)) {
         throw new IllegalStateException(String.format("Field '%s' is mandatory but no value was provided.", field.getName()));
      }

      if (StringUtils.trimToNull(value) != null) {
         Class<?> fieldType = field.getType();
         field.setAccessible(true);
         if (fieldType.equals(Boolean.TYPE)) {
            field.setBoolean(bean, Boolean.valueOf(value.trim()));
         } else if (fieldType.equals(Byte.TYPE)) {
            field.setByte(bean, Byte.valueOf(value.trim()));
         } else if (fieldType.equals(Double.TYPE)) {
            field.setDouble(bean, Double.valueOf(value.trim()));
         } else if (fieldType.equals(Float.TYPE)) {
            field.setFloat(bean, Float.valueOf(value.trim()));
         } else if (fieldType.equals(Integer.TYPE)) {
            field.setInt(bean, Integer.parseInt(value.trim()));
         } else if (fieldType.equals(Long.TYPE)) {
            field.setLong(bean, Long.parseLong(value.trim()));
         } else if (fieldType.equals(Short.TYPE)) {
            field.setShort(bean, Short.valueOf(value.trim()));
         } else if (fieldType.equals(Character.TYPE)) {
            field.setChar(bean, value.charAt(0));
         } else if (fieldType.isAssignableFrom(String.class)) {
            field.set(bean, value);
         } else {
            throw new IllegalStateException(String.format("Unable to set field value for field '%s' with value '%s' " +
                    "- type is unsupported. Use primitive and String types only.", fieldType, value));
         }
      }
   }
}