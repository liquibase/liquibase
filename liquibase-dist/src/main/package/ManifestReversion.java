import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Used by re-version.sh. Not included in normal compile so it doesn't accidentally end up in artifacts.
 * Need it to be a java class to handle weird manifest lines
 */
public class ManifestReversion {
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        String version = args[1];
        Manifest manifest;
        try (InputStream input = new FileInputStream(filename)) {
            manifest = new Manifest(input);
        }
        final Attributes attributes = manifest.getMainAttributes();

        attributes.putValue("Liquibase-Version", version);
        attributes.putValue("Bundle-Version", version);
        attributes.putValue("Import-Package", attributes.getValue("Import-Package").replaceAll("version=\"\\[0\\.0,1\\)\"", "version=\"["+version+",1)"));
        attributes.putValue("Export-Package", attributes.getValue("Export-Package").replaceAll(";version=\"0\\.0\\.0\"", ";version=\""+version+"\""));

        try (OutputStream out = new FileOutputStream(filename)) {
            manifest.write(out);
        }
    }
}
