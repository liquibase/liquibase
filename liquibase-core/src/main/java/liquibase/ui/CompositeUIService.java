package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Beta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used for handling multiple UI output services.
 */
@Beta
public class CompositeUIService extends AbstractExtensibleObject implements UIService {
    public final List<UIService> outputServices = new ArrayList<>();
    public final UIService inputService; // Because we are only one liquibase, we can only prompt with a single service

    public CompositeUIService(UIService inputService, Collection<UIService> outputServices) {
        this.inputService = inputService;
        this.outputServices.addAll(outputServices);
    }

    public List<UIService> getOutputServices() {
        return outputServices;
    }

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public void sendMessage(String message) {
        outputServices.forEach(service -> service.sendMessage(message));
    }

    @Override
    public void sendErrorMessage(String message) {
        outputServices.forEach(service -> service.sendErrorMessage(message));
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
        outputServices.forEach(service -> service.sendErrorMessage(message, exception));
    }

    @Override
    public <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
        return inputService.prompt(prompt, valueIfNoEntry, inputHandler, type);
    }

    @Override
    public void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
        inputService.setAllowPrompt(allowPrompt);
    }

    @Override
    public boolean getAllowPrompt() {
        return inputService.getAllowPrompt();
    }
}
