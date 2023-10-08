# Simple kotlin multipart library

Small library for working with multipart content type in http protocol

<!-- TOC -->
* [How to add library to you project](#how-to-add-library-to-you-project)
* [Main classes](#main-classes)
<!-- TOC -->

### How to add library to you project

#### Maven

```xml

<dependency>
    <groupId>io.github.edmondantes</groupId>
    <artifactId>simple-kotlin-multipart</artifactId>
    <version>${simple_library_version}</version>
</dependency>
```

#### Gradle (kotlin)

```kotlin
implementation("io.github.edmondantes:simple-kotlin-multipart:${simple_library_version}")
```

#### Gradle (groovy)

```groovy
implementation "io.github.edmondantes:simple-kotlin-multipart:${simple_library_version}"
```

### Main classes

Class `io.github.edmondantesx.multipart.Multipart` is main representation of multipart content type.

For transformation this class you can use `io.github.edmondantes.multipart.impl.DefaultMultipartEncoderDecoder`