package dev.m4nd3l.easysaves.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.Document;
import org.bson.io.BasicOutputBuffer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;

public class SerializationProcessor {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new YAMLMapper();
    private static final ObjectMapper xmlMapper = new XmlMapper();

    @SuppressWarnings("unchecked")
    public static byte[] processToBytes(Object object, StoringSystem system) throws Exception {
        String typeName = system.name();
        if (typeName.contains("JSON")) return jsonMapper.writeValueAsBytes(object);
        if (typeName.contains("YAML")) return yamlMapper.writeValueAsBytes(object);
        if (typeName.contains("XML")) return xmlMapper.writeValueAsBytes(object);
        if (typeName.contains("PROPERTIES")) {
            Properties properties = new Properties();
            if (object instanceof Map) properties.putAll((Map<?, ?>) object);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            properties.store(outputStream, null);
            return outputStream.toByteArray();
        }
        if (typeName.contains("BINARY_JAVA")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) { objectOutputStream.writeObject(object); }
            return outputStream.toByteArray();
        }
        if (typeName.contains("BSON")) {
            BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
            DocumentCodec documentCodec = new DocumentCodec();
            Document document = new Document((Map<String, Object>) jsonMapper.convertValue(object, Map.class));
            documentCodec.encode(new BsonBinaryWriter(outputBuffer), document, EncoderContext.builder().build());
            return outputBuffer.toByteArray();
        }
        throw new UnsupportedOperationException("Unsupported storing layout");
    }

    @SuppressWarnings("unchecked")
    public static <T> T processFromBytes(byte[] bytes, Class<T> type, StoringSystem system) throws Exception {
        String typeName = system.name();
        if (typeName.contains("JSON")) return jsonMapper.readValue(bytes, type);
        if (typeName.contains("YAML")) return yamlMapper.readValue(bytes, type);
        if (typeName.contains("XML")) return xmlMapper.readValue(bytes, type);
        if (typeName.contains("PROPERTIES")) {
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(bytes));
            return (T) jsonMapper.convertValue(properties, type);
        }
        if (typeName.contains("BINARY_JAVA")) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) { return type.cast(objectInputStream.readObject()); }
        }
        if (typeName.contains("BSON")) {
            DocumentCodec documentCodec = new DocumentCodec();
            Document document = documentCodec.decode(new BsonBinaryReader(ByteBuffer.wrap(bytes)), DecoderContext.builder().build());
            return jsonMapper.convertValue(document, type);
        }
        throw new UnsupportedOperationException("Unsupported parsing layout");
    }
}