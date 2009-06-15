package org.liquibase.intellij.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

public class IntellijResourceAccessor implements ResourceAccessor {
    public InputStream getResourceAsStream(String file) throws IOException {
        if (file == null) {
            return null;
        }
        VirtualFile virtualFile = findVirtualFile(file);
        if (virtualFile == null) {
            return null;
        }
        return virtualFile.getInputStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        if (packageName == null) {
            return null;
        }

        VirtualFile virtualFile = findVirtualFile(packageName);

        if (virtualFile == null) {
            return null;
        }

        final Iterator<VirtualFile> iterator = Arrays.asList(virtualFile.getChildren()).iterator();
        return new Enumeration<URL>() {
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public URL nextElement() {
                VirtualFile file = iterator.next();
                try {
                    return new URL(file.getPresentableUrl());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private VirtualFile findVirtualFile(final String file) {
        WriteAction action = new WriteAction(file);
        ApplicationManager.getApplication().runWriteAction(action);
        return action.returnValue;
    }

    public ClassLoader toClassLoader() {
        return null; //todo
    }

    private static class WriteAction implements Runnable {
        private VirtualFile returnValue;
        private String file;

        private WriteAction(String file) {
            this.file = file;
        }

        public void run() {
            returnValue = VirtualFileManager.getInstance().refreshAndFindFileByUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, file));
        }

    }

}
