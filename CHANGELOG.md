# Release Notes

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased (6.1.0-SNAPSHOT)

## [6.0.0] - 2025-06-25
* First jakarta release

## [5.0.3] - 2024-12-07
* Update library versions.
* Remove last traces of elastic search from class names. Make some more generic.

## [5.0.2] - 2024-12-26
* Update library versions.

## [5.0.1] - 2023-12-07
* Update library versions.
* Add dependency on latest snakeyaml to avoid CVEs
* Move Indexing mbean out of opensearch package to remove unnecessary dependencies.
* Fix restore of BwGeo - stored as BigDecimal value
* Add code to query opensearch to get scroll context info.

## [5.0.0] - 2022-02-12
* Update library versions.
* Use bedework-parent
* Add user authentication to elasticsearch
* Switch from ElasticSearch to OpenSearch because of license issues.
* Whole bunch of changes to handle ssl for opensearch.

## [4.1.4] - 2021-09-14
* Update library versions.

## [4.1.3] - 2021-09-11
* Update library versions.

## [4.1.2] - 2021-06-07
* Update library versions.

## [4.1.1] - 2021-05-30
* Update library versions.

## [4.1.0] - 2020-03-20
* Split off from bw-util
* Removed filters parameter from postGetEvents and associated methods - not used.


