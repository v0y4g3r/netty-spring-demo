# A minimal Netty Spring demo

## Introduction
This is a sample code of Netty with [Spring framework](https://projects.spring.io/spring-framework/) as the IOC container. Spring IOC greatly helps us from manually managing components and dependencies.

This code is very simple. The server listens for incoming connections on port 4444 and prints every message it received along with the sender.
The clients waits for user input and sends it to server. If user type "quit"(case insensitive), the client exits.

## Build
```
mvn package
java -jar target/netty-string-echo-1.0-SNAPSHOT.jar
```



