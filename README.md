# State Machine Input Output Processing
[![Latest Version](https://img.shields.io/maven-central/v/com.adobe.statelanguage/input-output-processing.svg?maxAge=3600&label=Latest%20Release)](https://search.maven.org/search?q=g:com.adobe.statelanguage%20a:input-output-processing)
[![JAVA](https://img.shields.io/badge/MADE%20with-JAVA-RED.svg)](#JAVA)
[![GitHub stars](https://img.shields.io/github/stars/adobe/state-machine-input-output-processing.svg?style=social&label=Star&maxAge=2592000)](https://github.com/adobe/state-machine-input-output-processing/stargazers/)

### Introduction
A Java implementation of Amazon State Language JSON Input and Output Processing.
Please refer https://states-language.net/spec.html#filters for Amazon States Language Specifications.

### Usage
The example of [`JsonProcessorTest`](src/test/java/com/adobe/statelanguage/JsonProcessorTest.java), demonstrates the usage of the `JsonProcessor`.

Use the following Maven artifact:
```xml
<dependency>
    <groupId>com.adobe.statelanguage</groupId>
    <artifactId>input-output-processing</artifactId>
    <version>...</version>
</dependency>
```

### Build
To build this project, you need JDK 11 or higher, and Maven:

    mvn clean install
    
### Test
The project contains Unit tests for basic functionality testing. 

### Contributing
Contributions are welcomed! Read the [Contributing Guide](CONTRIBUTING.md) for more information.

### Licensing
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
