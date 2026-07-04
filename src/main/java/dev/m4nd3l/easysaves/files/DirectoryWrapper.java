package dev.m4nd3l.easysaves.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Insulated wrapping module mapping operational lifecycle commands targeting folders directories.
 */
public class DirectoryWrapper {
    private final File directory;

    public DirectoryWrapper(File directory) { this.directory = directory; }
    public DirectoryWrapper(Path directoryPath) { this.directory = directoryPath.toFile(); }
    public DirectoryWrapper(String... path) { this(new File(String.join(File.separator, path))); }
    public DirectoryWrapper(DirectoryWrapper directory, String... path) { this(new File(directory.getFile(), String.join(File.separator, path))); }

    public Path getPath() { return directory.toPath(); }
    public File getFile() { return directory; }
    public String getAbsolutePath() { return directory.getAbsolutePath(); }
    public String getName() { return directory.getName(); }

    public Path getParentPath() { File parent = getParentFile(); return parent != null ? parent.toPath() : null; }
    public File getParentFile() { return directory.getParentFile(); }
    public DirectoryWrapper getParent() { File parent = getParentFile(); return parent != null ? new DirectoryWrapper(parent) : null; }

    public boolean exists() { return directory.exists(); }
    public boolean isDirectory() { return directory.isDirectory(); }
    public boolean create() { return directory.mkdirs(); }
    public boolean delete() { return deleteRecursively(directory); }
    public void deleteOnExit() { directory.deleteOnExit(); }

    public FileWrapper resolveFile(String... path) { return new FileWrapper(getAbsolutePath(), String.join(File.separator, path)); }
    public DirectoryWrapper resolveDirectory(String... path) { return new DirectoryWrapper(getAbsolutePath(), String.join(File.separator, path)); }

    /**
     * Lists all child files sitting directly within this directory level.
     *
     * @return A list containing tracked FileWrapper structures.
     */
    public List<FileWrapper> listFiles() {
        File[] files = directory.listFiles();
        if (files == null) return new ArrayList<>();
        List<FileWrapper> wrappers = new ArrayList<>();
        for (File current : files) if (current.isFile()) wrappers.add(new FileWrapper(current));
        return wrappers;
    }

    /**
     * Lists all child subdirectories sitting directly within this directory level.
     *
     * @return A list containing tracked DirectoryWrapper structures.
     */
    public List<DirectoryWrapper> listDirectories() {
        File[] files = directory.listFiles();
        if (files == null) return new ArrayList<>();
        List<DirectoryWrapper> wrappers = new ArrayList<>();
        for (File current : files) if (current.isDirectory()) wrappers.add(new DirectoryWrapper(current));
        return wrappers;
    }

    private boolean deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteRecursively(child);
        return file.delete();
    }

    /**
     * Spawns an empty file tracking instance within the current directory structure.
     *
     * @param name Name configuration target of the file.
     * @return Created FileWrapper asset references hook.
     * @throws IOException If construction errors out.
     */
    public FileWrapper addFile(String name) throws IOException {
        FileWrapper fileWrapper = resolveFile(name);
        fileWrapper.createEmpty();
        return fileWrapper;
    }

    /**
     * Spawns an empty subdirectory structure inside the current directory target scope.
     *
     * @param name Name configuration target of the folder.
     * @return Created DirectoryWrapper asset tracking references instance hook.
     */
    public DirectoryWrapper addDirectory(String name) {
        DirectoryWrapper directoryWrapper = resolveDirectory(name);
        directoryWrapper.create();
        return directoryWrapper;
    }

    public boolean removeFile(String name) { return resolveFile(name).delete(); }
    public boolean removeDirectory(String name) { return resolveDirectory(name).delete(); }
    public FileWrapper getFile(String name) { return resolveFile(name); }
    public DirectoryWrapper getDirectory(String name) { return resolveDirectory(name); }
}