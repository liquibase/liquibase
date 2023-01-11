package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class Connection implements HubModel {

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

    public Connection setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Connection setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }


    public String getName() {
        return name;
    }

    public Connection setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Connection setDescription(String description) {
        this.description = description;

        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Connection setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Connection setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public Date getRemoveDate() {
        return removeDate;
    }

    public Connection setRemoveDate(Date removeDate) {
        this.removeDate = removeDate;
        return this;
    }


    public Project getProject() {
        return project;
    }

    public Connection setProject(Project project) {
        this.project = project;
        return this;
    }

    @Override
    public String toString() {
        return "Connection " + jdbcUrl + " (" + id + ")";
    }
}
