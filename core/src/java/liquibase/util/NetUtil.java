package liquibase.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetUtil {

    /**
     * Smarter way to get localhost than InetAddress.getLocalHost.  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
     */
    public static InetAddress getLocalHost() throws UnknownHostException, SocketException {
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
        return lch;
    }

}
