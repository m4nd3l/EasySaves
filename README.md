## EasySaves

**EasySaves** is a flexible, lightweight Java storage and serialization wrapper designed to seamlessly handle application properties, configuration objects, and folder structures. It manages locations across platforms natively and provides hardware-fingerprinted encryption out of the box.

## ✨ Features

* 🚀 **Multi-Format Support:** Under-the-hood support for JSON, YAML, XML, Properties, BSON, and Java Native Binary serialization.
* 🛡️ **Hardware-Linked Security:** Transparently encrypt values or complete files via AES-GCM using keys dynamically derived from local machine hardware characteristics.
* 🛠️ **Fluid IO Abstractions:** Includes `FileWrapper` and `DirectoryWrapper` utility modules to ease local storage structure operations.
* ⚡ **Thread-Safe Runtime Tracking:** Tracks configurations and synchronized assets concurrently using thread-safe structures.

--

 ## 📦 Installation


Add the following dependency to your `build.gradle`:
```gradle
dependencies {
    implementation 'io.github.m4nd3l:EasySaves:1.0.0'
}
```
Or for Maven `pom.xml`:
```xml
<dependency>
    <groupId>io.github.m4nd3l</groupId>
    <artifactId>EasySaves</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🚀 Quick Start

### 1. Initialize Runtime Settings

Initialize the system once at application launch using the custom configuration builder fluent interface:

```java
import dev.m4nd3l.easysaves.EasySaves;
import dev.m4nd3l.easysaves.settings.EasySavesSettings;
import dev.m4nd3l.easysaves.settings.SavingLocations;
import dev.m4nd3l.easysaves.settings.StoringSystem;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        EasySavesSettings settings = EasySavesSettings.Builder.builder()
                .appName("MyAwesomeApp")
                .configFileName("settings.json")
                .location(SavingLocations.APPDATA)
                .storingSystem(StoringSystem.JSON_STRING)
                .build();

        EasySaves.init(settings);
    }
}

```

### 2. Managing Generic Application Configurations

Quickly track text configurations using basic key/value methods:

```java
// Store standard key-value pairs
EasySaves.addSetting("theme", "dark");
EasySaves.addSetting("volume", "80");

// Read standard keys safely
String currentTheme = EasySaves.getSetting("theme", "light");

```

### 3. Transparent Hardware-Bound Encryption

Encrypt values cleanly based on the host OS platform footprint signature:

```java
// Encrypts text instantly using derived hardware metadata
EasySaves.addSecureSetting("api_token", "secret_token_123");

// Automatically decrypts values back into plaintext
String token = EasySaves.getSecureSetting("api_token");

```

### 4. Explicit Object Serialization & File Wrapper APIs

Read or write structured data structures using `FileWrapper` handles anywhere inside your workspace directory layout:

```java
import dev.m4nd3l.easysaves.EasySaves;
import dev.m4nd3l.easysaves.files.FileWrapper;
import java.io.IOException;

public class SaveDataDemo {
    public void executeDemo() throws IOException {
        UserData user = new UserData("m4nd3l", 42);
        
        // Resolve a file wrapper handle cleanly inside the application root environment directory
        FileWrapper saveFile = EasySaves.getFile(true, "saves", "player_data.dat");
        
        // Serialize object models down to disk effortlessly 
        EasySaves.serialize(saveFile, user);

        // Deserializes and maps object models back from files cleanly
        UserData loadedUser = EasySaves.deserialize(saveFile, UserData.class);
    }
}

```

---

## ⚙️ Supported Storage Systems & Storage Rules

You can adjust formatting strategies dynamically within the settings builder by providing structural flags:

| StoringSystem Mode | Encoding Core Style | Cryptography Configuration | Best Used For |
| --- | --- | --- | --- |
| `JSON_STRING` | Jackson Object Text Mapper | Plain Text String | Readable, standard key value structures. |
| `YAML_STRING` | Jackson YAML Layout Engine | Plain Text String | Readable configuration tracking files. |
| `BSON` | MongoDB Binary Document Codec | Compressed Raw Byte Layout | Complex structural object documents. |
| `BINARY_JAVA` | Native Runtime JVM Stream | Compressed Raw Byte Layout | Deep JVM type storage. |
| `ENCRYPTED_JSON_STRING` | Jackson Mapper + AES-GCM Base64 | Encrypted Text String | Secure standard properties metadata. |
| `ENCRYPTED_BSON` | MongoDB Codec + AES-GCM Block | Encrypted Raw Byte Layout | High efficiency secure database files. |

## 📄 License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

## 👤 Author

**m4nd3l** - [GitHub](https://m4nd3l.github.io)

```
