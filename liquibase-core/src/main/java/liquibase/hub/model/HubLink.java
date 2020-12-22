package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class HubLink implements HubModel {
    private UUID id;

    private String key;

    private String url;

    private Date createDate;

    @Override
    public UUID getId() {
        return null;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
