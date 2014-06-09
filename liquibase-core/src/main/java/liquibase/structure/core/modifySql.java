package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;


public class modifySql extends AbstractDatabaseObject {

    private String name;

    public modifySql() {
    }

    public modifySql( String name ) {
        setName( name );
    }
    
    public Relation getRelation() {
        return getAttribute("relation", Relation.class);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getRelation()
        };
    }

    public modifySql setRelation(Relation relation) {
        setAttribute("relation", relation);

        return this;
    }


    @Override
    public Schema getSchema() {
        Relation relation = getRelation();
        if (relation == null) {
            return null;
        }
        return relation.getSchema();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public modifySql setName(String name) {
        this.name = name;
        setAttribute("name", name);

        return this;
    }

    public String getdbms() {
        return getAttribute("dbms", String.class);
    }

    public modifySql setdbms(String dbms) {
        setAttribute("dbms", dbms);

        return this;
    }

}

