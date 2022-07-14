package liquibase.resource;

public abstract class AbstractResource implements Resource {

    private final String path;

    public AbstractResource(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return getPath();
    }
}
