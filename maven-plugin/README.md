# OpenJAX JJB Maven Plugin

> Maven Plugin for [jJB][jjb] framework

[![Build Status](https://travis-ci.org/openjax/jjb.png)](https://travis-ci.org/openjax/jjb)

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

### JavaDocs

JavaDocs are available [here](https://jjb.openjax.org/apidocs/).

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

[jjb]: /
[mvn-plugin]: https://img.shields.io/badge/mvn-plugin-lightgrey.svg