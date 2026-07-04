package dev.m4nd3l.easysaves;

import dev.m4nd3l.easysaves.files.DirectoryWrapper;
import dev.m4nd3l.easysaves.files.FileWrapper;
import dev.m4nd3l.easysaves.security.EasySavesSecurity;
import dev.m4nd3l.easysaves.settings.EasySavesSettings;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EasySaves {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private static EasySavesSettings settings;
    private static DirectoryWrapper mainFolder;
    private static FileWrapper defaultConfigFile;
    private static Map<String, DirectoryWrapper> subFolders;
    private static Map<String, FileWrapper> subFiles;
    private static Map<String, String> config;

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

        config = defaultConfigJustCreated ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(settings.deserialize(defaultConfigFile, Map.class));
    }

    public static void addSetting(String key, String value) {
        config.put(key, value);
        settings.serialize(defaultConfigFile, config);
    }

    public static String getSetting(String key) { return config.get(key); }
    public static String getSetting(String key, String defaultValue) { return config.getOrDefault(key, defaultValue); }
    public static void removeSetting(String key) { config.remove(key); settings.serialize(defaultConfigFile, config); }
    public static boolean hasSetting(String key) { return config.containsKey(key); }
    public static void clearSettings() { config.clear(); settings.serialize(defaultConfigFile, config); }

    public static FileWrapper getFile(boolean createIfNull, String... path) throws IOException {
        FileWrapper file = new FileWrapper(mainFolder, String.join(File.separator, path));
        if (file.exists()) return file;
        if (createIfNull) { file.createParentFolders(); file.createEmpty(); resyncFiles(); return file; }
        return null;
    }

    public static DirectoryWrapper getDirectory(boolean createIfNull, String... path) {
        DirectoryWrapper dir = new DirectoryWrapper(mainFolder, String.join(File.separator, path));
        if (dir.exists()) return dir;
        if (createIfNull) { dir.create(); resyncFiles(); return dir; }
        return null;
    }

    public static boolean deleteFile(String... path) {
        FileWrapper file = new FileWrapper(mainFolder, String.join(File.separator, path));
        boolean result = file.delete();
        if (result) resyncFiles();
        return result;
    }

    public static boolean deleteDirectory(String... path) {
        DirectoryWrapper dir = new DirectoryWrapper(mainFolder, String.join(File.separator, path));
        boolean result = dir.delete();
        if (result) resyncFiles();
        return result;
    }

    public static void serialize(FileWrapper file, Object object) { settings.serialize(file, object); }
    public static <T> T deserialize(FileWrapper file, Class<T> type) { return settings.deserialize(file, type); }

    public static void resyncConfig() {
        String contentString = defaultConfigFile.readString();
        byte[] contentBytes = defaultConfigFile.readBytes();
        if (contentString == null || contentString.isEmpty() || contentBytes == null || contentBytes.length == 0) config = new ConcurrentHashMap<>();
        else config = new ConcurrentHashMap<>(settings.deserialize(defaultConfigFile, Map.class));
    }

    public static void resyncFiles() {
        subFolders.clear();
        subFiles.clear();
        for (DirectoryWrapper directory : mainFolder.listDirectories()) subFolders.put(directory.getName(), directory);
        for (FileWrapper file : mainFolder.listFiles()) subFiles.put(file.getName(), file);
    }

    public static void addSecureSetting(String key, String secretValue) {
        String encryptedValue = EasySavesSecurity.encrypt(secretValue);
        addSetting(key, encryptedValue);
    }

    public static String getSecureSetting(String key) {
        String encryptedValue = getSetting(key);
        if (encryptedValue == null) return null;
        return EasySavesSecurity.decrypt(encryptedValue);
    }

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