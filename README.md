# Simple kotlin multipart library

[![Maven Central](http://img.shields.io/maven-central/v/io.github.edmondantes/simple-kotlin-multipart?color=green&style=flat-square)](https://search.maven.org/search?q=g:io.github.edmondantes%20a:simple-kotlin-multipart)
[![GitHub](http://img.shields.io/github/license/Simple-Kotlin-Project/simple-kotlin-multipart?style=flat-square)](https://github.com/Simple-Kotlin-Project/simple-kotlin-multipart/blob/master/LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub Workflow Status (with branch)](https://img.shields.io/github/actions/workflow/status/Simple-Kotlin-Project/simple-kotlin-multipart/check.yml?branch=master&style=flat-square)](https://github.com/Simple-Kotlin-Project/simple-kotlin-multipart/actions/workflows/check.yml)

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