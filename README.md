# bw-util-index [![Build Status](https://travis-ci.org/Bedework/bw-util-index.svg)](https://travis-ci.org/Bedework/bw-util-index)

This project provides a number of utility classes and methods related to indexing for
[Bedework](https://www.apereo.org/projects/bedework).

## Requirements

1. JDK 11
2. Maven 3

## Building Locally

> mvn clean install

## Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release:

> mvn -P bedework-dev release:clean release:prepare

When prompted, select the desired version; accept the defaults for scm tag and next development version.
When the build completes, and the changes are committed and pushed successfully, execute:

> mvn -P bedework-dev release:perform

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).

## Release Notes
### 4.1.0
    * Split off from bw-util
    * Removed filters parameter from postGetEvents and associated methods - not used.

#### 4.1.1
* Update library versions

#### 4.1.2
* Update library versions

#### 4.1.3
* Update library versions

#### 4.1.4
* Update library versions

#### 5.0.0
* Use bedework parent
* Update library versions
* Add user authentication to elasticsearch
* Switch from ElasticSearch to OpenSearch because of licence issues.
* Whole bunch of changes to handle ssl for opensearch.

#### 5.0.1
* Update library versions
* Add dependency on latest snakeyaml to avoid CVEs
* Move Indexing mbean out of opensearch package to remove unnecessary dependencies.
* Fix restore of BwGeo - stored as BigDecimal value
* Add code to query opensearch to get scroll context info.
