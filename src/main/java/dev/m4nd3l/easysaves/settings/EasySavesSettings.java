package dev.m4nd3l.easysaves.settings;

import dev.m4nd3l.easysaves.files.FileWrapper;
import dev.m4nd3l.easysaves.security.EasySavesSecurity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EasySavesSettings {
    private String appName;
    private String configFileName;
    private SavingLocations location;
    private StoringSystem storingSystem;

    private EasySavesSettings(Builder builder) {
        appName = builder.appName;
        configFileName = builder.configFileName;
        location = builder.location;
        storingSystem = builder.storingSystem;
    }

    public String getAppName() { return appName; }
    public SavingLocations getLocation() { return location; }
    public StoringSystem getStoringSystem() { return storingSystem; }
    public String getConfigFileName() { return configFileName; }

    public void serialize(FileWrapper file, Object object) {
        try {
            byte[] rawBytes = SerializationProcessor.processToBytes(object, storingSystem);
            if (storingSystem.isEncrypted()) rawBytes = EasySavesSecurity.encrypt(rawBytes);

            if (storingSystem.isStringBased()) {
                String textOutput = storingSystem.isEncrypted() ? Base64.getEncoder().encodeToString(rawBytes) : new String(rawBytes, StandardCharsets.UTF_8);
                file.write(textOutput, false);
            }
            else file.write(rawBytes, false);
        } catch (Exception exception) { throw new RuntimeException("Serialization process halted unexpected", exception); }
    }

    public <T> T deserialize(FileWrapper file, Class<T> type) {
        try {
            byte[] processedBytes;
            if (storingSystem.isStringBased()) {
                String inputString = file.readString();
                if (inputString.isEmpty()) return null;
                processedBytes = storingSystem.isEncrypted() ? Base64.getDecoder().decode(inputString) : inputString.getBytes(StandardCharsets.UTF_8);
            } else {
                processedBytes = file.readBytes();
                if (processedBytes.length == 0) return null;
            }

            if (storingSystem.isEncrypted()) processedBytes = EasySavesSecurity.decrypt(processedBytes);
            return SerializationProcessor.processFromBytes(processedBytes, type, storingSystem);
        } catch (Exception exception) { throw new RuntimeException("Deserialization process halted unexpected", exception); }
    }

    public static final class Builder {
        private String appName = "JavaApp";
        private String configFileName = "config.cfg";
        private SavingLocations location = SavingLocations.APPDATA;
        private StoringSystem storingSystem = StoringSystem.JSON_STRING;

        public static Builder builder() { return new Builder(); }
        public Builder appName(String appName) { this.appName = appName; return this; }
        public Builder configFileName(String configFileName) { this.configFileName = configFileName; return this; }
        public Builder location(SavingLocations location) { this.location = location; return this; }
        public Builder storingSystem(StoringSystem storingSystem) { this.storingSystem = storingSystem; return this; }
        public EasySavesSettings build() { return new EasySavesSettings(this); }
    }
}
