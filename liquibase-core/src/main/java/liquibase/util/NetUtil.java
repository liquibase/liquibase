package liquibase.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetUtil {

    /**
     * Smarter way to get localhost than InetAddress.getLocalHost.  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
     */
    private static InetAddress getLocalHost() throws UnknownHostException, SocketException {
        // Windows Vista returns the IPv6 InetAddress using this method, which is not
        // particularly useful as it has no name or useful address, just "0:0:0:0:0:0:0:1".
        // That is why windows should be treated differently to linux/unix and use the
        // default way of getting the localhost.
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("windows")) {
            return InetAddress.getLocalHost();
        }

        InetAddress lch = null;
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

        while (e.hasMoreElements()) {
            NetworkInterface i = e.nextElement();

            Enumeration<InetAddress> ie = i.getInetAddresses();
            if (!ie.hasMoreElements()) {
                break;
            }
            lch = ie.nextElement();
            if (!lch.isLoopbackAddress()) break;
        }
        return lch == null ? null : lch;
    }

    /**
     * Returns Local Host IP Address
     * @return local Host IP address
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static String getLocalHostAddress() throws UnknownHostException, SocketException {
        return getLocalHost().getHostAddress();
    }

    /**
     * Returns Local Host Name
     * @return local Host Name
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static String getLocalHostName() throws UnknownHostException, SocketException {
        return getLocalHost().getHostName();
    }


}
