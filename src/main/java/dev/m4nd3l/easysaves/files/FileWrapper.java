package dev.m4nd3l.easysaves.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileWrapper {
    private File file;

    public FileWrapper(File file) { this.file = file; }
    public FileWrapper(Path filePath) { this.file = new File(filePath.toUri()); }
    public FileWrapper(String... path) { this(new File(String.join(File.separator, path))); }
    public FileWrapper(FileWrapper file, String... path) { this(new File(file.getFile(), String.join(File.separator, path))); }
    public FileWrapper(DirectoryWrapper file, String... path) { this(new File(file.getFile(), String.join(File.separator, path))); }

    public Path getPath() { return file.toPath(); }
    public File getFile() { return file; }
    public String getAbsolutePath() { return file.getAbsolutePath(); }
    public String getName() { return file.getName(); }
    public DirectoryWrapper toDirectory() { return new DirectoryWrapper(getFile()); }

    public Path getParentPath() { File parent = getParentFile(); return parent != null ? parent.toPath() : null; }
    public File getParentFile() { return file.getParentFile(); }
    public String getParentAbsolutePath() { File parent = getParentFile(); return parent != null ? parent.getAbsolutePath() : ""; }
    public String getParentName() { File parent = getParentFile(); return parent != null ? parent.getName() : ""; }
    public FileWrapper getParent() { File parent = getParentFile(); return parent != null ? new FileWrapper(parent) : null; }
    public DirectoryWrapper getParentDirectory() { File parent = getParentFile(); return parent != null ? new DirectoryWrapper(parent) : null; }
    public boolean exists() { return file.exists(); }
    public boolean isDirectory() { return file.isDirectory(); }
    public boolean isFile() { return file.isFile(); }
    public boolean isExecutable() { return file.canExecute(); }
    public boolean isHidden() { return file.isHidden(); }
    public boolean canRead() { return file.canRead(); }
    public boolean canWrite() { return file.canWrite(); }

    public boolean setReadOnly() { return file.setReadOnly(); }
    public boolean setReadable(boolean readable) { return file.setReadable(readable); }
    public boolean setReadable(boolean readable, boolean ownerOnly) { return file.setReadable(readable, ownerOnly); }
    public boolean setWritable(boolean writable) { return file.setWritable(writable); }
    public boolean setWritable(boolean writable, boolean ownerOnly) { return file.setWritable(writable, ownerOnly); }
    public boolean setExecutable(boolean executable) { return file.setExecutable(executable); }
    public boolean setExecutable(boolean executable, boolean ownerOnly) { return file.setExecutable(executable, ownerOnly); }

    public boolean createEmpty() throws IOException { return file.createNewFile(); }
    public boolean create(String content) throws IOException { boolean result = file.createNewFile(); writeIf(content, false, (_, _) -> result); return result; }
    public boolean create(byte[] content) throws IOException { boolean result = file.createNewFile(); writeIf(content, false, (_, _) -> result); return result; }
    public boolean delete() { return file.delete(); }
    public String deleteAndReadString() { String content = readString(); delete(); return content; }
    public byte[] deleteAndReadBytes() { byte[] content = readBytes(); delete(); return content; }
    public void deleteOnExit() { file.deleteOnExit(); }

    public boolean createFolders() { return file.mkdirs(); }
    public boolean createParentFolders() { return getParentFile().mkdirs(); }

    public void write(String content, boolean append) {
        try {
            createParentFolders();
            Files.writeString(
                    file.toPath(),
                    content,
                    StandardOpenOption.CREATE,
                    append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) { System.err.println(e); }
    }

    public void write(byte[] content, boolean append) {
        try {
            createParentFolders();
            Files.write(
                    file.toPath(),
                    content,
                    StandardOpenOption.CREATE,
                    append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) { System.err.println(e); }
    }

    public String readString() {
        if (!file.exists()) return "";
        try { return Files.readString(file.toPath()); }
        catch (Exception _) { return ""; }
    }

    public byte[] readBytes() {
        if (!file.exists()) return new byte[0];
        try { return Files.readAllBytes(file.toPath()); }
        catch (Exception _) { return new byte[0]; }
    }

    public void writeIf(String content, boolean append, DoublePredicate<String, FileWrapper> condition) { if (condition.test(content, this)) write(content, append); }
    public void writeIf(byte[] content, boolean append, DoublePredicate<byte[], FileWrapper> condition) { if (condition.test(content, this)) write(content, append); }
}
