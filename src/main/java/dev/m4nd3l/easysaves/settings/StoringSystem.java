package dev.m4nd3l.easysaves.settings;

/**
 * Format layout structures supported for data processing, indicating encryption configurations and layout types.
 */
public enum StoringSystem {
    JSON_STRING,
    PROPERTIES_STRING,
    XML_STRING,
    YAML_STRING,

    BINARY_JAVA,
    BSON,
    JSON_UTF8_BYTES,
    PROPERTIES_UTF8_BYTES,
    XML_UTF8_BYTES,
    YAML_UTF_BYTES,

    ENCRYPTED_JSON_STRING,
    ENCRYPTED_PROPERTIES_STRING,
    ENCRYPTED_XML_STRING,
    ENCRYPTED_YAML_STRING,

    ENCRYPTED_BINARY_JAVA,
    ENCRYPTED_BSON,
    ENCRYPTED_JSON_UTF8_BYTES,
    ENCRYPTED_PROPERTIES_UTF8_BYTES,
    ENCRYPTED_XML_UTF8_BYTES,
    ENCRYPTED_YAML_UTF_BYTES;

    public boolean isEncrypted() { return name().startsWith("ENCRYPTED_"); }
    public boolean isBinaryOrBytes() { return name().endsWith("_BYTES") || name().endsWith("_JAVA") || name().endsWith("PROTOBUF") || name().endsWith("BSON"); }
    public boolean isStringBased() { return !isBinaryOrBytes(); }
}