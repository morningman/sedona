# Sedona Functions for Apache Doris

## Compile

sh build.sh

## Generate Doris DDL

```shell
sh run-generate-ddl.sh --global --jarpath="file:///path/to/sedona-doris-1.8.0-SNAPSHOT.jar" --outfile create.sql
sh run-generate-ddl.sh --global --jarpath="file:///path/to/sedona-doris-1.8.0-SNAPSHOT.jar" --outfile drop.sql --drop
sh run-generate-ddl.sh --global --jarpath="file:///path/to/sedona-doris-1.8.0-SNAPSHOT.jar" --outfile show.sql --show
```
