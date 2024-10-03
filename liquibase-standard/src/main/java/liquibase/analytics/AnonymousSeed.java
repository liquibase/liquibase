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

public class AnonymousSeed {
    private String username;
    private String macAddresses;
    private String machineId;

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
        // Sort the list in case the mac addresses are returned in a different order at different times
        Collections.sort(macAddresses);
        return StringUtils.join(macAddresses);
    }

    public UUID generateId() {
        // If we only have a username, then we don't have robust enough seed data so use a random ID.
        if (StringUtils.isEmpty(machineId) && StringUtils.isEmpty(macAddresses)) {
            return UUID.randomUUID();
        }
        return UUID.nameUUIDFromBytes(StringUtils.join(username, macAddresses, machineId).getBytes());
    }
}
