package org.liquibase.intellij.plugin;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import liquibase.FileOpener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

public class IntellijFileOpener implements FileOpener {
    public InputStream getResourceAsStream(String file) throws IOException {
        VirtualFile virtualFile = findVirtualFile(file);
        if (virtualFile == null) {
            return null;
        }
        return virtualFile.getInputStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
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

    private VirtualFile findVirtualFile(String file) {
        return VirtualFileManager.getInstance().refreshAndFindFileByUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, file));
    }

}
