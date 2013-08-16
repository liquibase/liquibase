package liquibase.change;

public @interface DatabaseChangeNote {
    public String database() default "";
    public String notes() default "";
}
