package liquibase.include;

import liquibase.plugin.AbstractPluginFactory;

public class IncludeServiceFactory extends AbstractPluginFactory<IncludeService> {

 private IncludeServiceFactory() {}

 @Override
 protected Class<IncludeService> getPluginClass() {
	return IncludeService.class;
 }

 @Override
 protected int getPriority(IncludeService obj, Object... args) {
	return obj.getPriority();
 }

 public IncludeService getIncludeService() {
	return getPlugin();
 }

}
