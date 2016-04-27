package liquibase.integration.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.StopWatch;

import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class AsyncSpringLiquibase extends SpringLiquibase {

	/** Classe logger */
	private final Logger log = new LogFactory().getLog("AsyncSpringLiquibase");
	/** run async */
	private boolean async = false;

	/*
	 * (non-Javadoc)
	 *
	 * @see liquibase.integration.spring.SpringLiquibase#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws LiquibaseException {
		if (async) {
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(new Runnable() {
				/*
				 * (non-Javadoc)
				 *
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					log.warning("Starting Liquibase asynchronously, your database might not be ready at startup.");
					final StopWatch watch = new StopWatch();
					watch.start();
					try {
						super.afterPropertiesSet();
					} catch (final LiquibaseException e) {
						log.severe("error while running liquibase", e);
					}
					watch.stop();
					log.debug("Started Liquibase in " + watch.getTotalTimeMillis() + " ms");
				}
			});
			executor.shutdown();
		} else {
			super.afterPropertiesSet();
		}
	}

	/**
	 * Enable async log
	 *
	 * @param async
	 *            flag allowing to ryun async
	 */
	public void setAsync(final boolean async) {
		this.async = async;
	}
}
