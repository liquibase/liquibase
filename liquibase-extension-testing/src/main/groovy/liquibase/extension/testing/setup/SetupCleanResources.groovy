package liquibase.extension.testing.setup

class SetupCleanResources extends TestSetup {

    private final List<String> resourcesToDelete = new ArrayList<>()

    SetupCleanResources(String[] resourcesToDelete) {
        this.resourcesToDelete.addAll(resourcesToDelete as Set)
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        for (String fileToDelete : resourcesToDelete) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(fileToDelete)
            if (url == null) {
                return
            }
            File f = new File(url.toURI())
            if (f.exists()) {
                boolean b = f.delete()
                if (b) {
                    assert !f.exists(): "The file '$f' was not deleted"
                }
            }
        }
    }
}
