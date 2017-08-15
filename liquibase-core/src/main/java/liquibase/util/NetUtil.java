package liquibase.util;

import liquibase.logging.LogService;
import liquibase.logging.LogType;

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
        if ((osName != null) && osName.toLowerCase().contains("windows")) {
            return InetAddress.getLocalHost();
        }

        InetAddress loopback = null;
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface i = e.nextElement();
            if (i.isUp() && !i.isPointToPoint()) {
                Enumeration<InetAddress> ie = i.getInetAddresses();
                while (ie.hasMoreElements()) {
                    InetAddress lch = ie.nextElement();
                    if (lch.isLoopbackAddress()) {
                        loopback = lch;
                    } else if (!lch.isLinkLocalAddress()) {
                        return lch;
                    }
                }
            }
        }
        return loopback;
    }

    /**
     * Returns Local Host IP Address
     * @return local Host IP address
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static String getLocalHostAddress() throws UnknownHostException, SocketException {
        try {
            return getLocalHost().getHostAddress();
        } catch (Exception e) {
            LogService.getLog(NetUtil.class).debug(LogType.LOG, "Error getting hostname", e);
            return "unknown";
        }
    }

    /**
     * Returns Local Host Name
     * @return local Host Name
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static String getLocalHostName() throws UnknownHostException, SocketException {
        try {
            return getLocalHost().getHostName();
        } catch (Exception e) {
            LogService.getLog(NetUtil.class).debug(LogType.LOG, "Error getting hostname", e);
            return "unknown";
        }
    }


}
