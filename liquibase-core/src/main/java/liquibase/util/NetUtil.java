package liquibase.util;

import liquibase.Scope;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetUtil {

    private static InetAddress localHost;

    /**
     * Smarter way to get localhost than InetAddress.getLocalHost.  See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4665037
     */
    private static InetAddress getLocalHost() throws UnknownHostException, SocketException {
        if (localHost == null) {
            InetAddress foundHost = InetAddress.getLocalHost();

            if (foundHost.isLoopbackAddress() || foundHost.isLinkLocalAddress()) {
                //try to find something other than localhost

                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while (e.hasMoreElements()) {
                    NetworkInterface i = e.nextElement();
                    if (i.isUp() && !i.isPointToPoint()) {
                        Enumeration<InetAddress> ie = i.getInetAddresses();
                        while (ie.hasMoreElements()) {
                            InetAddress lch = ie.nextElement();
                            if (!lch.isLoopbackAddress() && !lch.isLinkLocalAddress()) {
                                localHost = lch;
                                return localHost;
                            }
                        }
                    }
                }
            }

            localHost = foundHost;
        }
        return localHost;
    }

    /**
     * @return Machine's IP address
     */
    public static String getLocalHostAddress() throws UnknownHostException, SocketException {
        try {
            return getLocalHost().getHostAddress();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(NetUtil.class).fine("Error getting hostname", e);
            return "unknown";
        }
    }

    /**
     * @return Machine's host name
     */
    public static String getLocalHostName() throws UnknownHostException, SocketException {
        try {
            return getLocalHost().getHostName();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(NetUtil.class).fine("Error getting hostname", e);
            return "unknown";
        }
    }


}
