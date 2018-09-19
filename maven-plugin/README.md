<img src="https://images.cooltext.com/5195722.png" align="right">

## jjb-maven-plugin<br>![mvn-plugin][mvn-plugin] <a href="https://www.openjax.org/"><img src="https://img.shields.io/badge/OpenJAX--blue.svg"></a>
> Maven Plugin for [jJB][jjb] framework

### Introduction

The `jjb-maven-plugin` plugin is used to generate JSON bindings with the [jJB][jjb] framework.

### Goals Overview

* [`jjb:generate`](#jjbgenerate) generates jJB bindings.

### Usage

#### `jjb:generate`

The `jjb:generate` goal is bound to the `generate-sources` phase, and is used to generate jJB bindings for jJB documents in the `manifest`. To configure the generation of jJB bindings for desired jJB schemas, add a `manifest` element to the plugin's configuration.

##### Example

```xml
<plugin>
  <groupId>org.openjax.jjb</groupId>
  <artifactId>jjb-maven-plugin</artifactId>
  <version>0.9.8-SNAPSHOT</version>
  <configuration>
    <destDir>${project.build.directory}/generated-sources/jjb</destDir>
    <schemas>
      <schema>src/main/resources/json.jsonx</schema>
    </schemas>
  </configuration>
</plugin>
```

#### Configuration Parameters

| Name              | Type    | Use      | Description                                                                   |
|:------------------|:--------|:---------|:------------------------------------------------------------------------------|
| `/`               | Object  | Required | Manifest descriptor.                                                          |
| `/destDir`        | String  | Required | Destination path of generated bindings.                                       |
| `/schemas`        | List    | Required | List of `resource` elements.                                                  |
| `/schemas/schema` | String  | Required | File path of XML Schema.                                                      |

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

<a href="http://cooltext.com" target="_top"><img src="https://cooltext.com/images/ct_pixel.gif" width="80" height="15" alt="Cool Text: Logo and Graphics Generator" border="0" /></a>

[jjb]: /
[mvn-plugin]: https://img.shields.io/badge/mvn-plugin-lightgrey.svg