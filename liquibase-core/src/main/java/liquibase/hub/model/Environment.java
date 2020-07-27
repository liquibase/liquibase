package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class Environment {

    private UUID id;
    private String jdbcUrl;
    private String name;
    private String description;
    private Date createDate;
    private Date updateDate;
    private Date removeDate;

    private Project project;

    public UUID getId() {
        return id;
    }

    public Environment setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Environment setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }


    public String getName() {
        return name;
    }

    public Environment setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Environment setDescription(String description) {
        this.description = description;

        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Environment setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Environment setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public Date getRemoveDate() {
        return removeDate;
    }

    public Environment setRemoveDate(Date removeDate) {
        this.removeDate = removeDate;
        return this;
    }


    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "Environment " + jdbcUrl + " (" + id + ")";
    }
}
