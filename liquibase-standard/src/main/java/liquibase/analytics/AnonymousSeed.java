package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class generates an anonymous seed based on various system properties
 * such as the username, MAC addresses, and machine ID. It uses this data
 * to generate a UUID that can serve as an identifier.
 */
public class AnonymousSeed {
    private String username;
    private String macAddresses;
    private String machineId;

    /**
     * Constructs an AnonymousSeed object. It attempts to retrieve the current
     * username, the machine ID (on Linux systems), and the MAC addresses
     * of network interfaces.
     * If the machine ID is available, it is preferred over MAC addresses.
     */
    public AnonymousSeed() {
        username = SystemProperties.getUserName();

        try {
            if (SystemUtils.IS_OS_LINUX) {
                machineId = FileUtils.readFileToString(new File("/etc/machine-id"));
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(AnalyticsTrackEvent.class).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Failed to determine /etc/machine-id when calculating user ID", e);
        }

        // Only check for MAC addresses if machineId is null. Machine ID is more robust (and likely faster to calculate than mac addresses)
        try {
            if (StringUtils.isEmpty(machineId)) {
                macAddresses = getMacAddresses();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(AnalyticsTrackEvent.class).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "Failed to determine mac addresses when calculating user ID", e);
        }
    }

    /**
     * Retrieves the MAC addresses of all network interfaces on the machine.
     * This method sorts the MAC addresses to ensure consistent ordering.
     *
     * @return a concatenated string of MAC addresses
     * @throws SocketException if there is an error accessing network interfaces
     */
    private static String getMacAddresses() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        List<String> macAddresses = new ArrayList<>();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            byte[] macArray = network.getHardwareAddress();
            if (ArrayUtils.isNotEmpty(macArray)) {
                macAddresses.add(StringUtils.toEncodedString(macArray, StandardCharsets.UTF_8));
            }
        }
        // Sort the list in case the MAC addresses are returned in a different order at different times.
        Collections.sort(macAddresses);
        return StringUtils.join(macAddresses);
    }

    /**
     * Generates a UUID based on the collected system data (username, MAC addresses, machine ID).
     * If neither the machine ID nor MAC addresses are available, a random UUID is generated instead.
     *
     * @return a UUID generated from the system data, or a random UUID if insufficient data is available
     */
    public String generateId() {
        // If we only have a username, then we don't have robust enough seed data, so use a random ID.
        if (StringUtils.isEmpty(machineId) && StringUtils.isEmpty(macAddresses)) {
            return "random-" + UUID.randomUUID();
        }
        return String.valueOf(UUID.nameUUIDFromBytes(StringUtils.join(username, macAddresses, machineId).getBytes()));
    }
}
