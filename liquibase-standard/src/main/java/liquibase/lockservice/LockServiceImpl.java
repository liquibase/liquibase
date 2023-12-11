package liquibase.lockservice;

/**
 * LockServiceImpl has been renamed to StandardLockService. This stub class exists for backwards comparability
 *
 * @deprecated use StandardLockService instead
 */
public class LockServiceImpl extends StandardLockService {
    @Override
    public int getPriority() {
        return super.getPriority()-1;
    }
}
