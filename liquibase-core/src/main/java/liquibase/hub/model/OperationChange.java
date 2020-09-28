package liquibase.hub.model;

import liquibase.changelog.ChangeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OperationChange implements HubModel {
    private Project project;
    private Operation operation;
    private List<ChangeSet> changeSets = new ArrayList<>();

    @Override
    public UUID getId() {
        return null;
    }

    public Project getProject() {
        return project;
    }

    public OperationChange setProject(Project project) {
        this.project = project;
        return this;
    }

    public Operation getOperation() {
        return operation;
    }

    public OperationChange setOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }
}
