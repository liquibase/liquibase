package liquibase.integration.spring;

import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.lockservice.LockServiceFactory;

import java.util.Arrays;
import java.util.Optional;

public enum RegistrableFactoryHandler {

    LOCK_SERVICE_FACTORY(LockServiceFactory.class, LockServiceFactory.getInstance()),
    CHANGE_LOG_HISTORY_SERVICE_FACTORY(ChangeLogHistoryServiceFactory.class, ChangeLogHistoryServiceFactory.getInstance());

    private Class<? extends Registrable> clz;
    private Registrable registrableFactory;

    RegistrableFactoryHandler(Class<? extends Registrable> clz, Registrable registrableFactory) {
        this.clz = clz;
        this.registrableFactory = registrableFactory;
    }

    @Override
    public String toString() {
        return "RegistrableFactoryHandler{" +
                "clz=" + clz +
                ", registrableFactory=" + registrableFactory +
                '}';
    }

    public Class<? extends Registrable> getClz() {
        return clz;
    }

    public void setClz(Class<? extends Registrable> clz) {
        this.clz = clz;
    }

    public Registrable getRegistrableFactory() {
        return registrableFactory;
    }

    public void setRegistrableFactory(Registrable registrableFactory) {
        this.registrableFactory = registrableFactory;
    }

    public static Registrable fromFactoryClz(Class<Registrable> clzName) {
        final Optional<RegistrableFactoryHandler> find = Arrays.stream(RegistrableFactoryHandler.values())
                .filter(factoryHandler -> factoryHandler.clz.equals(clzName))
                .findFirst();

        return find.map(RegistrableFactoryHandler::getRegistrableFactory).orElse(null);
    }
}
