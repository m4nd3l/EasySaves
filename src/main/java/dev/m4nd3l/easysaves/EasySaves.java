package dev.m4nd3l.easysaves;

import dev.m4nd3l.easysaves.files.DirectoryWrapper;
import dev.m4nd3l.easysaves.files.FileWrapper;
import dev.m4nd3l.easysaves.security.EasySavesSecurity;
import dev.m4nd3l.easysaves.settings.EasySavesSettings;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for managing application settings, configuration files, and directories.
 * Provides static utilities for secure and standard serialization/deserialization workflows.
 */
public class EasySaves {
    private static EasySavesSettings settings;
    private static DirectoryWrapper mainFolder;
    private static FileWrapper defaultConfigFile;
    private static Map<String, DirectoryWrapper> subFolders;
    private static Map<String, FileWrapper> subFiles;
    private static Map<String, String> config;

    /**
     * Initializes the saving system with the provided settings layout.
     * Creates necessary root folders and loads existing configurations into memory.
     *
     * @param settings The configuration settings to apply.
     * @throws IOException If file system operations fail.
     */
    @SuppressWarnings("unchecked")
    public static void init(EasySavesSettings settings) throws IOException {
        EasySaves.settings = settings;
        mainFolder = new DirectoryWrapper(settings.getLocation().getFolder(settings.getAppName()));
        defaultConfigFile = new FileWrapper(mainFolder, settings.getConfigFileName());

        boolean defaultConfigJustCreated = !defaultConfigFile.exists();

        if (!mainFolder.exists()) mainFolder.create();
        if (!defaultConfigFile.exists()) defaultConfigFile.createEmpty();

        subFolders = new ConcurrentHashMap<>();
        subFiles = new ConcurrentHashMap<>();

        resyncFiles();
        Map<String, String> map = settings.deserialize(defaultConfigFile, Map.class);
        config = defaultConfigJustCreated  || map == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(map);
    }

    /**
     * Adds or updates a standard text configuration value and flushes it to disk.
     *
     * @param key   The configuration key.
     * @param value The value to bind.
     */
    public static void addSetting(String key, String value) {
        config.put(key, value);
        settings.serialize(defaultConfigFile, config);
    }

    /**
     * Retrieves a standard configuration value.
     *
     * @param key The configuration key.
     * @return The bound value, or null if missing.
     */
    public static String getSetting(String key) { return config.get(key); }

    /**
     * Retrieves a standard configuration value, falling back to a default if missing.
     *
     * @param key          The configuration key.
     * @param defaultValue The fallback value.
     * @return The bound value or the default fallback.
     */
    public static String getSetting(String key, String defaultValue) { return config.getOrDefault(key, defaultValue); }

    /**
     * Removes a configuration key and flushes changes to disk.
     *
     * @param key The key to wipe out.
     */
    public static void removeSetting(String key) { config.remove(key); settings.serialize(defaultConfigFile, config); }

    /**
     * Checks if a configuration key exists in the current settings map.
     *
     * @param key The key to look up.
     * @return True if the key is present.
     */
    public static boolean hasSetting(String key) { return config.containsKey(key); }

    /**
     * Clears all configurations and overrides the configuration file on disk to empty.
     */
    public static void clearSettings() { config.clear(); settings.serialize(defaultConfigFile, config); }

    /**
     * Fetches or builds a specific file within the application root directory.
     *
     * @param createIfNull True to create the file structure instantly if missing.
     * @param path         The relative path tokens pointing to the file.
     * @return The mapped FileWrapper instance, or null.
     * @throws IOException If the file creation fails.
     */
    public static FileWrapper getFile(boolean createIfNull, String... path) throws IOException {
        FileWrapper file = new FileWrapper(mainFolder, String.join(File.separator, path));
        if (file.exists()) return file;
        if (createIfNull) { file.createParentFolders(); file.createEmpty(); resyncFiles(); return file; }
        return null;
    }

    /**
     * Fetches or builds a specific directory within the application root directory.
     *
     * @param createIfNull True to construct the directory structure if missing.
     * @param path         The relative path tokens pointing to the directory.
     * @return The mapped DirectoryWrapper instance, or null.
     */
    public static DirectoryWrapper getDirectory(boolean createIfNull, String... path) {
        DirectoryWrapper dir = new DirectoryWrapper(mainFolder, String.join(File.separator, path));
        if (dir.exists()) return dir;
        if (createIfNull) { dir.create(); resyncFiles(); return dir; }
        return null;
    }

    /**
     * Deletes a targeted file within the application scope and resynchronizes cache tracking.
     *
     * @param path The relative path tokens pointing to the file.
     * @return True if the deletion succeeded.
     */
    public static boolean deleteFile(String... path) {
        FileWrapper file = new FileWrapper(mainFolder, String.join(File.separator, path));
        boolean result = file.delete();
        if (result) resyncFiles();
        return result;
    }

    /**
     * Deletes a targeted directory within the application scope and resynchronizes cache tracking.
     *
     * @param path The relative path tokens pointing to the directory.
     * @return True if the deletion succeeded.
     */
    public static boolean deleteDirectory(String... path) {
        DirectoryWrapper dir = new DirectoryWrapper(mainFolder, String.join(File.separator, path));
        boolean result = dir.delete();
        if (result) resyncFiles();
        return result;
    }

    /**
     * Serializes an object instance directly into a given wrapper file target.
     *
     * @param file   The target file wrapper.
     * @param object The object to store.
     */
    public static void serialize(FileWrapper file, Object object) { settings.serialize(file, object); }

    /**
     * Deserializes a targeted file wrapper back into a concrete Object type structure.
     *
     * @param <T>  The target object type.
     * @param file The file wrapper source.
     * @param type The matching class type framework.
     * @return The parsed object data instance.
     */
    public static <T> T deserialize(FileWrapper file, Class<T> type) { return settings.deserialize(file, type); }

    /**
     * Re-reads the default config system file from disk to manually refresh memory structures.
     */
    @SuppressWarnings("unchecked")
    public static void resyncConfig() {
        String contentString = defaultConfigFile.readString();
        byte[] contentBytes = defaultConfigFile.readBytes();
        if (contentString == null || contentString.isEmpty() || contentBytes == null || contentBytes.length == 0) config = new ConcurrentHashMap<>();
        else config = new ConcurrentHashMap<>(settings.deserialize(defaultConfigFile, Map.class));
    }

    /**
     * Scans the base main folder directory to update local tracking lists of child assets.
     */
    public static void resyncFiles() {
        subFolders.clear();
        subFiles.clear();
        for (DirectoryWrapper directory : mainFolder.listDirectories()) subFolders.put(directory.getName(), directory);
        for (FileWrapper file : mainFolder.listFiles()) subFiles.put(file.getName(), file);
    }

    /**
     * Encrypts a sensitive string value and stores it securely inside the configuration file.
     *
     * @param key         The configuration key.
     * @param secretValue The plaintext value to encrypt.
     */
    public static void addSecureSetting(String key, String secretValue) {
        String encryptedValue = EasySavesSecurity.encrypt(secretValue);
        addSetting(key, encryptedValue);
    }

    /**
     * Retrieves an encrypted configuration value and transparently decrypts it.
     *
     * @param key The configuration key.
     * @return The plaintext string value, or null if missing.
     */
    public static String getSecureSetting(String key) {
        String encryptedValue = getSetting(key);
        if (encryptedValue == null) return null;
        return EasySavesSecurity.decrypt(encryptedValue);
    }

    /**
     * Retrieves an encrypted configuration value and decrypts it, falling back to a default value if missing.
     *
     * @param key          The configuration key.
     * @param defaultValue The fallback value if missing.
     * @return The decrypted plaintext string value, or the default value.
     */
    public static String getSecureSetting(String key, String defaultValue) {
        String encryptedValue = getSetting(key);
        if (encryptedValue == null) return defaultValue;
        return EasySavesSecurity.decrypt(encryptedValue);
    }

    public static Map<String, DirectoryWrapper> getSubFolders() { return subFolders; }
    public static Map<String, FileWrapper> getSubFiles() { return subFiles; }
    public static DirectoryWrapper getMainFolder() { return mainFolder; }
    public static FileWrapper getDefaultConfigFile() { return defaultConfigFile; }
}