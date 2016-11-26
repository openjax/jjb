<img src="http://safris.org/logo.png" align="right" />
# XJB [![CohesionFirst](http://safris.org/cf2.svg)](https://cohesionfirst.com/)
> eXtended JSON Binding

## Introduction

XJB is a lightweight framework based on a XSD [XJB Schema](http://xws.safris.org/xjb.xsd) that allows one to create a schema for JSON classes. To its disadvantage, JavaScript is a highly non-cohesive language, resulting in errors always realized in runtime. As there does not exist a formalized schema specification for JSON, developers often make repeated mistakes when designing JSON messages, encoding JSON objects, and decoding JSON strings. XJB presents a schema model that can be used to bring errors to edit-time and compile-time, greatly reducint the risk of the same errors to appear in run-time.

## Why XJB?

### CohesionFirst™

Developed with the CohesionFirst™ approach, XJB is the cohesive alternative to the creation of JSON classes that offers validation and fail-fast execution. Made possible by the rigorous conformance to design patterns and best practices in every line of its implementation, XJB is a complete solution for the creation and management of a JSON interface model, both for consumers and producers.

### Cohesive Binding Between JSON and Java Classes

XJB uses a `json.xjb` file that conforms to the [XJB Schema](http://xws.safris.org/xjb.xsd) to generate Java beans to bind the JSON classes to Java. The generated classes are strongly typed and offer the full benefits of a cohesive interface to JSON objects in Java. The generated classes can be used to parse and marshal JSON messages, confident that all messages conform to the definition in the XJB.

### Support Complete JSON Spec and Abstract Types

The [XJB Schema](http://xws.safris.org/xjb.xsd) has constructs that allow for the definition of the entire range of possible JSON structures. Additionally, the schema offers abstract types, which provides one with the ability to use the OO principles of inheritance and polymorphism for JSON -- powerful paradigms which are not used in JSON as it is based on the "loosely Object Oriented" language of JavaScript.

### Support Check Constraint Triggers on Encode and Decode

The [XJB Schema](http://xws.safris.org/xjb.xsd) offers a semantics for the definition of check constraints on properties. Properties of different types have different check constraint classes available. The `String` property has a `PatternValidator` that allows a developer to assert the property to conform to a regex pattern. Additionally, all properties have semantics for `nullable` and `required`, where the former states whether a property can be `null`, and the latter states whether the property can altogether be omitted from the message.

### Validating and Fail-Fast

XJB is based on the [XJB Schema](http://xws.safris.org/xjb.xsd) that allows one to define JSON classes in XML. The XJB XSD uses the full power of the XML Validation to provide immediate feedback of errors or inconsistencies in the model. Cross-object and cross-property relations are checked using the `key`, `keyref` and `unique` facets of the XML Schema specification. Once a `json.xjb` passes the validation checks, it is guaranteed to produce JSON-compliant objects.

## Getting Started

### Prerequisites

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) - The minimum required JDK version.
* [Maven](https://maven.apache.org/) - The dependency management system used to install XDL.

### Example

1. In your preferred development directory, create a [`maven-archetype-quickstart`](http://maven.apache.org/archetypes/maven-archetype-quickstart/) project.

  ```tcsh
  mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
  ```

2. Add the `mvn.repo.safris.org` Maven repositories to the POM.

  ```xml
  <repositories>
    <repository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </pluginRepository>
  </pluginRepositories>
  ```

3. Create a `json.xjb` and put it in `src/main/resources/`.

  ```xml
  <json name="json"
    xmlns="http://cf.safris.org/xjb.xsd"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://cf.safris.org/xjb.xsd http://cf.safris.org/xjb.xsd">

    <description>JSON class definitions for communication protocol of server API</description>

    <object name="id" abstract="true">
      <property xsi:type="string" name="id" pattern="[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}" null="false"/>
    </object>

    <object name="idVersion" abstract="true" extends="id">
      <property xsi:type="number" name="version" null="false"/>
    </object>

    <object name="ids">
      <property xsi:type="string" name="id" pattern="[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}" array="true" null="false"/>
    </object>

    <object name="credentials">
      <property xsi:type="string" name="email" pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}" null="false"/>
      <property xsi:type="string" name="password" pattern="[0-9a-f]{64}" required="false" null="false"/>
    </object>

    <object name="account" extends="credentials">
      <property xsi:type="string" name="id" pattern="[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}" null="false" required="false"/>
      <property xsi:type="string" name="firstName" null="false"/>
      <property xsi:type="string" name="lastName" null="false"/>
    </object>

  </json>
  ```

4. Add the [`org.safris.maven.plugin`:`xjb-maven-plugin`](https://github.com/SevaSafris/xjb-maven-plugin) to the POM.

  ```xml
  <plugin>
    <groupId>org.safris.maven.plugin</groupId>
    <artifactId>xjb-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
      <execution>
        <phase>generate-sources</phase>
        <goals>
          <goal>generate</goal>
        </goals>
        <configuration>
          <manifest xmlns="http://maven.safris.org/common/manifest.xsd">
            <destdir>${project.build.directory}/generated-sources/xjb</destdir>
            <schemas>
              <schema>${basedir}/src/main/resources/json.xjb</schema>
            </schemas>
          </manifest>
        </configuration>
      </execution>
    </executions>
  </plugin>
  ```

5. Add the `org.safris.xws`:`xjb` dependency to the POM.

  ```xml
  <dependency>
    <groupId>org.safris.cf</groupId>
    <artifactId>xjb</artifactId>
    <version>1.1.3</version>
  </dependency>
  ```

6. Upon successful execution of the `org.safris.xws`:`xjb` plugin, a class by the name of `json` (as was specified in the `name` attribute of the `<json>` element in `json.xjb) will be generated in `generated-sources/xjb`. Add this path to your Build Paths in your IDE to integrate into your project.

7. The generated classes can be instantiated as any other Java objects. They are strongly typed, and will guide you in proper construction of a JSON message. The following patterns can be used for parsing and marshalling XJB to and from JSON:

  To parse JSON to XJB: `final json.Credentials credentials = (json.Credentials)JSObject.parse(rawType, new StringReader("{email: 'john@doe', password: '066b91577bc547e21aa329c74d74b0e53e29534d4cc0ad455abba050121a9557'}"))`
  
  To marshal XJB to JSON: `System.out.println(credentials.toString());`

### Integration with XRS

XJB can be used as the `MessageBodyReader` and `MessageBodyWriter` to marshal and parse JSON objects in a JAX-RS 2.0 server. Please [see here](#getting-started-1) for an example of how to initiate XJB as a Provider for your JAX-RS 2.0 application. The [XRS implementation](https://github.com/SevaSafris/xrs) offers a CohesionFirst™ alternative to JAX-RS 2.0.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
