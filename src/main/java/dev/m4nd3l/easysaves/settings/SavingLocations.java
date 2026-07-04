package dev.m4nd3l.easysaves.settings;

import dev.m4nd3l.easysaves.files.DirectoryWrapper;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum SavingLocations {
    APPDATA(System.getenv("APPDATA") != null ? System.getenv("APPDATA") : System.getProperty("user.home")),
    LOCAL_APPDATA(System.getenv("LOCALAPPDATA") != null ? System.getenv("LOCALAPPDATA") : System.getProperty("user.home")),
    COMMON_APPDATA(System.getenv("ProgramData") != null ? System.getenv("ProgramData") : "/var/lib"),

    USER_HOME(System.getProperty("user.home")),
    DOCUMENTS(Paths.get(System.getProperty("user.home"), "Documents").toString()),
    DOWNLOADS(Paths.get(System.getProperty("user.home"), "Downloads").toString()),
    DESKTOP(Paths.get(System.getProperty("user.home"), "Desktop").toString()),

    TEMP(System.getProperty("java.io.tmpdir")),
    CURRENT_WORKING_DIRECTORY(System.getProperty("user.dir"));

    private Path mainFolder;
    SavingLocations(String mainFolderPath) { mainFolder = Paths.get(mainFolderPath); }
    public DirectoryWrapper getFolder(String appName) { return new DirectoryWrapper(Paths.get(mainFolder.toAbsolutePath().toString(), appName)); }
}
