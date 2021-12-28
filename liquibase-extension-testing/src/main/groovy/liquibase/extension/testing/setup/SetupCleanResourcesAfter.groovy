package liquibase.extension.testing.setup


import liquibase.extension.testing.TestDatabaseConnections

class SetupCleanResourcesAfter extends TestSetup {

    private final List<String> resourcesToDelete = new ArrayList<>()
    private FilenameFilter filter

    /**
     *
     * Delete resources using a filter
     * The list of resources to delete is a list of directories
     *
     * @param filter
     * @param resourcesToDelete
     */
    SetupCleanResourcesAfter(FilenameFilter filter, String[] resourcesToDelete) {
        this.filter = filter
        this.resourcesToDelete.addAll(resourcesToDelete as Set)
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        //
        // Empty implementation because only the cleanup method is used
        //
    }

    @Override
    void cleanup() throws Exception {
        //
        // Delete resources in the specified directories that match the filter
        //
        if (filter != null) {
            deleteResourcesThatMatch()
            return
        }

        //
        // Delete the list of resources
        //
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

    void deleteResourcesThatMatch() {
        for (String resourceName : resourcesToDelete) {
            File directory = new File(resourceName)
            if (! directory.isAbsolute()) {
                URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName)
                if (url == null) {
                    continue
                }
                directory = new File(url.toURI())
            }
            assert directory.isDirectory(): "The resource '$resourceName' is not a directory"
            String[] listOfFiles = directory.list(filter)
            for (String s : listOfFiles) {
                File f = new File(directory, s)
                if (f.exists()) {
                    boolean b = f.delete()
                    if (b) {
                        assert !f.exists(): "The file '$f' was not deleted"
                    }
                }
            }
        }
    }
}
