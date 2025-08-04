package liquibase.command.copy;

import liquibase.plugin.Plugin;

import java.io.File;

/**
 *
 * Copy project files from a source location to a target location
 *
 */
public interface ProjectCopier extends Plugin {

   /**
    *
    * Check the path to see if the implementation supports it
    *
    * @param   path                The path to check
    * @return  int
    *
    */
   int getPriority(String path);

   /**
    *
    * Return true if this ProjectCopier works with remote locations false if not
    *
    * @return            boolean
    *
    */
   boolean isRemote();

   /**
    *
    * Create a local directory that can be used as an intermediate area to store files
    * to be copied.
    *
    * @param   target                  The target location for the copy
    * @param   keepTempFiles           True if the temp files should be kept false if not
    * @return  File                    The local diectory
    *
    */
   File createWorkingStorage(String target, boolean keepTempFiles);

   /**
    *
    * Copy files from the source location to the target
    *
    * @param source            The source location
    * @param target            The target location
    * @param recursive         Recurse through the source location if tre
    *
    */
   void copy(String source, String target, boolean recursive);
}