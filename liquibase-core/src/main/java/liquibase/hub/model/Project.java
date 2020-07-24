package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class Project {

    private UUID id;
    private String name;
    private Date createDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Project " + getId() + " (" + getName() + ")";
    }
}
