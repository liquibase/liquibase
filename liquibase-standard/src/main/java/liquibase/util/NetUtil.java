package liquibase.util;

import liquibase.Scope;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetUtil {

    private static InetAddress localHost;
    private static String hostName;

    private static final String UNKNOWN_HOST_NAME = "unknown";

    private NetUtil() {}

    /**
     * Smarter way to get localhost than InetAddress.getLocalHost.
     * @see <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4665037">view bug</a>
     */
    private static InetAddress getLocalHost() throws UnknownHostException, SocketException {
        if (localHost == null) {
            InetAddress foundHost = InetAddress.getLocalHost();

            if (foundHost == null || foundHost.isLoopbackAddress() || foundHost.isLinkLocalAddress()) {
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
    public static String getLocalHostAddress() {
        try {
            InetAddress localHost = getLocalHost();
            if(localHost != null) {
                return localHost.getHostAddress();
            }
            else {
                return UNKNOWN_HOST_NAME;
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(NetUtil.class).fine("Error getting hostname", e);
            return UNKNOWN_HOST_NAME;
        }
    }

    /**
     * @return Machine's host name. This method can be better to call than getting it off {@link #getLocalHost()} because sometimes the external address returned by that function does not have a useful hostname attached to it.
     * This function will make sure a good value is returned.
     */
    public static String getLocalHostName() {
        if (hostName == null ) {
            try {
                InetAddress localHost = getLocalHost();
                if(localHost != null) {
                    hostName = localHost.getHostName();
                    if (hostName.equals(localHost.getHostAddress())) {
                        //sometimes the external IP interface doesn't have a hostname associated with it but localhost always does
                        InetAddress lHost = InetAddress.getLocalHost();
                        if (lHost != null) {
                            hostName = lHost.getHostName();
                        }
                    }
                }
                else {
                    hostName = UNKNOWN_HOST_NAME;
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(NetUtil.class).fine("Error getting hostname", e);
                if (hostName == null) {
                    hostName = UNKNOWN_HOST_NAME;
                }
            }
        }
        return hostName;
    }


}
