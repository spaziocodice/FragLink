<p><img src="https://github.com/spaziocodice/FragLink/assets/7569632/62918ccc-54af-472b-8732-acfce92a1adf"/></p>
<br/>

FragLink enables [Linked Data Fragments](https://linkeddatafragments.org) capabilities to your Server. It's not a server itself. Instead, it comes as a SpringBoot autoconfigure module that you can easily plug into your RDF Server.

## Quick Start
FragLink is written in Java and makes use of the Spring Framework. Specifically, it is an autoconfigure module, which is supposed to be declared as a dependency in a SpringBoot (Server) project.  

### 1. Create a SpringBoot project
This will be your Linked Data Fragment Server. It is strongly recommended to use [Spring initializr](https://start.spring.io/) to define the initial shape of the module (e.g., components, dependencies, frameworks, starters). 

### 2. Maven repository and Fraglink dependency.
Then, once the project skeleton is created, open the pom.xml (in case you're using Gradle, there's a corresponding configuration) and add the following section:

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
  <version>1.1.1</version>
</dependency>
```

### 3. Configuration
Assuming you already set up everything in your SpringBoot module (e.g., dependencies and so on), here's the minimal configuration required by FragLink:

```yaml
fraglink:
  base:
    url: https://fragments.yourproject.org  (this is an example)
  page:
    maxStatements: 50 (the maximum number of statements returned in response)
  dataset:
    name: "The dataset project/name" 
    description: "An optional description of the project."
```

### 4. Start
Start your server. After a few seconds, you should see the following messages:

```
... : <FRAGLINK-00001> : FragLink v1.1.1 has been enabled on this server.
```

The server is running: great! Linked Data Fragments are exposed through the root (/) REST endpoint. The endpoint template is 

```
https://fragments.yourproject.org{subject, predicate, object, graph, page}
```

However, being a triple/quad pattern resolver, it doesn't still know how to fetch data. The default implementation is a simple NoOp, meaning no data is returned in response, only metadata.
Here's an example (empty) response: 

```
<https://fragments.yourproject.org/fragments#metadata> {
    <https://fragments.yourproject.org/fragments#dataset>
            a       <http://rdfs.org/ns/void#Dataset> , <http://www.w3.org/ns/hydra/core#Collection>;
            <http://rdfs.org/ns/void#subset>
                    <https://fragments.yourproject.org/fragments>;
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
                      <http://www.w3.org/ns/hydra/core#template>
                              "https://fragments.yourproject.org/fragments{?subject,predicate,object,page}";
                      <http://www.w3.org/ns/hydra/core#variableRepresentation>
                              <http://www.w3.org/ns/hydra/core#ExplicitRepresentation>
                    ] .
    
    <https://fragments.yourproject.org#dataset>
            a       <http://www.w3.org/ns/hydra/core#Collection>;
            <http://purl.org/dc/elements/1.1/description>
                    "An optional description of the dataset.";
            <http://purl.org/dc/elements/1.1/title>
                    "The Dataset project/name";
            <http://www.w3.org/ns/hydra/core#member>
                    <https://fragments.yourproject.org/fragments#dataset> .
    
    <https://fragments.yourproject.org/fragments#metadata>
            <http://xmlns.com/foaf/0.1/primaryTopic>
                    [ a <https://fragments.yourproject.org/fragments> ] .
    
    <https://fragments.yourproject.org/fragments>
            a       <http://www.w3.org/ns/hydra/core#PartialCollectionView>;
            <http://purl.org/dc/elements/1.1/description>
                    "Linked Data Fragment of Share-VDE dataset containing triples matching the pattern {?s ?p ?o ?q}"@en;
            <http://purl.org/dc/elements/1.1/source>
                    "https://fragments.yourproject.org/fragments#dataset";
            <http://purl.org/dc/elements/1.1/title>
                    "Linked Data Fragment of The Share-VDE Project Dataset"@en;
            <http://rdfs.org/ns/void#subset>
                    <https://fragments.yourproject.org/fragments>;
            <http://rdfs.org/ns/void#triples>
                    "0"^^<http://www.w3.org/2001/XMLSchema#long>;
            <http://www.w3.org/ns/hydra/core#firstPage>
                    <https://fragments.yourproject.org/fragments?page=1>;
            <http://www.w3.org/ns/hydra/core#itemsPerPage>
                    "50"^^<http://www.w3.org/2001/XMLSchema#int>;
            <http://www.w3.org/ns/hydra/core#totalItems>
                    "0"^^<http://www.w3.org/2001/XMLSchema#long> .
}
```

### 4. Linked Data Fragment Resolver
To create a valid binding in your project tied to your data source, you must create an implementation of  
  
`com.spaziocodice.labs.fraglink.service.impl.LinkedDataFragmentResolver`  

The interface contains a single method, which takes a triple/quad pattern in input and expects the list of matching triples in output.


