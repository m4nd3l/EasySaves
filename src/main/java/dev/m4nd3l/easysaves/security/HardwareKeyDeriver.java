package dev.m4nd3l.easysaves.security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Generates an environment-linked hardware fingerprint key using operating system attributes to construct localized cryptography hooks.
 */
public class HardwareKeyDeriver {

    /**
     * Resolves machine identifiers across OS structures to form an integrated unique fingerprint key.
     *
     * @return A Base64 SHA-256 string representing the hardware profile signature block.
     */
    public static String generateMasterKey() {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String hardwareRawData = "";

        if (operatingSystem.contains("win")) hardwareRawData = getWindowsFingerprint();
        else if (operatingSystem.contains("mac")) hardwareRawData = getMacFingerprint();
        else hardwareRawData = getLinuxFingerprint();

        if (hardwareRawData.isEmpty()) hardwareRawData = System.getProperty("user.name") + System.getProperty("os.arch") + Runtime.getRuntime().availableProcessors();
        return hashFingerprint(hardwareRawData);
    }

    private static String getWindowsFingerprint() {
        String motherboard = executeCommand("wmic baseboard get serialnumber");
        String cpu = executeCommand("wmic cpu get processorid");
        return motherboard.trim() + cpu.trim();
    }

    private static String getMacFingerprint() {
        return executeCommand("ioreg -l | grep IOPlatformSerialNumber");
    }

    private static String getLinuxFingerprint() {
        String machineId = executeCommand("cat /etc/machine-id");
        if (machineId.isEmpty()) machineId = executeCommand("cat /var/lib/dbus/machine-id");
        return machineId;
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) if (!line.trim().isEmpty() && !line.toLowerCase().contains("serialnumber") && !line.toLowerCase().contains("processorid")) output.append(line.trim());
            }
            process.waitFor();
        } catch (Exception exception) { }
        return output.toString();
    }

    private static String hashFingerprint(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception exception) { throw new RuntimeException(exception); }
    }
}