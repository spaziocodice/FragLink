<p><img src="https://github.com/spaziocodice/FragLink/assets/7569632/7a28703a-508b-4e8b-94ac-263385f42b8c"/></p>
<br/>

FragLink enables [Linked Data Fragments](https://linkeddatafragments.org) capabilities to your Server.   
It's not a server itself. Instead, it comes as a SpringBoot autoconfigure module that you can easily plug into your RDF Server.

## Quick Start
FragLink is written in Java and makes use of the Spring Framework. Specifically, it is an autoconfigure module, which is supposed to be declared as a dependency in a SpringBoot (Server) project.  

### 1. Create a SpringBoot project
This will be your Linked Data Fragment Server. It is strongly recommended to use [Spring initializr](https://start.spring.io/) to define the initial shape of the module (e.g., components, dependencies, frameworks, starters). 
Then, once the project skeleton is created, open the pom.xml (in case you're using Gradle, there's a corresponding thing to be defined) and add the following section:

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
 
Assuming you already set up everything in your SpringBoot module (e.g. dependencies and so on), here's the minimal configuration required by FragLink:

```yaml
fraglink:
  base:
    url: https://fragments.yourproject.org  (this is an example)
  page:
    maxStatements: 50 (the maximum number of statements returned in response)
  dataset:
    name: "The dataset / project name" 
    description: "An optional description about the project"
```

Start you server, after few seconds you should see the following messages:

```
... : <FRAGLINK-00001> : FragLink v1.0.0-SNAPSHOT has been enabled on this server. Do not use a SNAPSHOT version in production!
```

The server is running: great! Linked Data Fragments are exposed through the root (/) REST endpoint. The endpoint template is 

```
https://fragments.yourproject.org{subject, predicate, object, graph, page}
```

However, being a triple/quad pattern resolver, it doesn't still know how to fetch data. The default implementation is a simple NoOp, meaning no data is returned in response, only metadata.
Here's an example (empty) response: 

```
<https://fragments.svde.org#metadata> {
    <https://fragments.svde.org#metadata>
            <http://xmlns.com/foaf/0.1/primaryTopic>
                    <https://fragments.svde.org#dataset> .
    
    <https://fragments.svde.org#dataset>
            a       <http://www.w3.org/ns/hydra/core#Collection> , <http://rdfs.org/ns/void#Dataset>;
            <http://purl.org/dc/terms/description>
                    "Share-VDE (SVDE) is a library-driven initiative which brings together the bibliographic catalogues and authority files of a community of libraries in a shared discovery environment based on linked data.";
            <http://purl.org/dc/terms/source>
                    "<https://fragments.svde.org#dataset>";
            <http://purl.org/dc/terms/title>
                    "The Share-VDE Project Dataset";
            <http://rdfs.org/ns/void#subset>
                    <https://fragments.svde.org>;
            <http://rdfs.org/ns/void#triples>
                    "10002392"^^<http://www.w3.org/2001/XMLSchema#long>;
            <http://www.w3.org/ns/hydra/core#member>
                    <https://fragments.svde.org#dataset>;
            <http://www.w3.org/ns/hydra/core#search>
                    [ <http://www.w3.org/ns/hydra/core#mapping>
                              [ <http://www.w3.org/ns/hydra/core#property>
                                        <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject>;
                                <http://www.w3.org/ns/hydra/core#variable>
                                        "subject"
                              ];
                      <http://www.w3.org/ns/hydra/core#mapping>
                              [ <http://www.w3.org/ns/hydra/core#property>
                                        <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate>;
                                <http://www.w3.org/ns/hydra/core#variable>
                                        "predicate"
                              ];
                      <http://www.w3.org/ns/hydra/core#mapping>
                              [ <http://www.w3.org/ns/hydra/core#property>
                                        <http://www.w3.org/1999/02/22-rdf-syntax-ns#object>;
                                <http://www.w3.org/ns/hydra/core#variable>
                                        "object"
                              ];
                      <http://www.w3.org/ns/hydra/core#mapping>
                              [ <http://www.w3.org/ns/hydra/core#property>
                                        "http://www.w3.org/ns/sparql-service-description#";
                                <http://www.w3.org/ns/hydra/core#variable>
                                        "object"
                              ];
                      <http://www.w3.org/ns/hydra/core#template>
                              "https://fragments.svde.org{subject,predicate,object,graph,page}"
                    ];
            <http://www.w3.org/ns/hydra/core#totalItems>
                    "0"^^<http://www.w3.org/2001/XMLSchema#long> .
    
    <https://fragments.svde.org/>
            a       <http://www.w3.org/ns/hydra/core#PagedCollection> , <http://www.w3.org/ns/hydra/core#Collection> , <http://www.w3.org/ns/hydra/core#PartialCollectionView>;
            <http://purl.org/dc/terms/source>
                    "<https://fragments.svde.org#dataset>";
            <http://purl.org/dc/terms/title>
                    "The Share-VDE Project Dataset: Linked Data Fragment";
            <http://rdfs.org/ns/void#subset>
                    "https://fragments.svde.org#dataset";
            <http://rdfs.org/ns/void#triples>
                    "0"^^<http://www.w3.org/2001/XMLSchema#long>;
            <http://www.w3.org/ns/hydra/core#firstPage>
                    <https://fragments.svde.org/?page=1>;
            <http://www.w3.org/ns/hydra/core#itemsPerPage>
                    "50"^^<http://www.w3.org/2001/XMLSchema#int>;
            <http://www.w3.org/ns/hydra/core#totalItems>
                    "0"^^<http://www.w3.org/2001/XMLSchema#long> .
}
```

To create a valid binding in your project tied to your data source, you must create an implementation of  
  
`com.spaziocodice.labs.fraglink.service.impl.LinkedDataFragmentResolver`  

See the Wiki for further details, and...have fun!


