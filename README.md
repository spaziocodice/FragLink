<p><img src="https://private-user-images.githubusercontent.com/7569632/291635678-9774b284-ec35-4d37-9941-6eebeb60a0a8.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTEiLCJleHAiOjE3MDMwMDM2MTMsIm5iZiI6MTcwMzAwMzMxMywicGF0aCI6Ii83NTY5NjMyLzI5MTYzNTY3OC05Nzc0YjI4NC1lYzM1LTRkMzctOTk0MS02ZWViZWI2MGEwYTgucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQUlXTkpZQVg0Q1NWRUg1M0ElMkYyMDIzMTIxOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyMzEyMTlUMTYyODMzWiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9MGNkODdmNzczMmZkMmJlZWQyZWZjMGJkMjEyOTVlNDY2YTE0NDgyYTkxMWNkOTAyMWI4OGNiYzBkYTRiZDdhZCZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QmYWN0b3JfaWQ9MCZrZXlfaWQ9MCZyZXBvX2lkPTAifQ.XH3QHWXXsVS5YREaxQpcWzg5ZHti71xaikXYjXbMKeE"/></p>
<br/>
FragLink enables [Linked Data Fragments](https://linkeddatafragments.org/) capabilities to your Server. 
It's not a server itself, instead, it comes as a SpringBoot autoconfigure module that you can easily plug into your RDF Server.

## Quick Start
FragLink is written in Java and makes use of the Spring Framework. Specifically, it is an autoconfigure module, which is supposed to be declared as a dependency in a SpringBoot (Server) project.  

### 1. Create your own SpringBoot project
This will be your Linked Data Fragment Server. It is strongly recommended to use [Spring initializr](https://start.spring.io/) for defining the initial shape of the module (e.g. components, dependencies, frameworks, starters). 
Then, once the project skeleton is created, open the pom.xml (in case you're using Gradle there's a corresponding thing to be defined) and add the following section:

```xml
<repositories>
  <repository>
    <id>fraglink-package-registry</id>
    <url>https://gitlab.com/api/v4/projects/52914288/packages/maven</url>
  </repository>
</repositories>
```

The snippet above declares the coordinates to the maven repository where the FragLink artifacts are hosted. 
Then, in the dependencies section: 


```xml
<dependency>
  <groupId>com.spaziocodice.labs.rdf</groupId>
  <artifactId>fraglink-starter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

That's all! 
Assuming you already set up everything in your SpringBoot module, after starting the server you should see the following messages:

```
2023-12-19 ... : <FRAGLINK-00001> : FragLink v1.0.0-SNAPSHOT has been enabled on this server. Do not use a SNAPSHOT version in production!
```
