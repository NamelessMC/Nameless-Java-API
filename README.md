# Nameless-Java-API

Java library for interacting with a NamelessMC website. It is used by for example the NamelessMC Minecraft server plugin and Nameless-Link Discord bot.

## Install to local maven repository

We don't publish builds to maven central (yet). You will need to build and install this project locally.

OpenJDK 11, git and maven should be installed. Run in a terminal:

```
git clone https://github.com/NamelessMC/Nameless-Java-API
cd Nameless-Java-API
mvn install
```

## Include as dependency

```xml
<dependency>
    <groupId>com.namelessmc</groupId>
    <artifactId>java-api</artifactId>
    <version>canary</version>
</dependency>
```

## Usage

Create an API instance:

```java
String apiUrl = "";
String apiKey = "";
NamelessAPI api = NamelessAPI.builder(apiUrl, apiKey).build();
```
