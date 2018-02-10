# Vesilasku

A POC for producing water billing reports from [Sontex](https://www.sontex.ch)
flow metering data. Currently, the application:

 * takes a csv of specifying the apartments and water meters
   ([example](src/test/resources/fi/kapsi/kosmik/vesilasku/report/report-apartments.csv))
 * takes a csv of Sontex data, typically named like 
   `DevicesValues646_12345678_2018-01-14-581.rlv`
   ([example](src/test/resources/fi/kapsi/kosmik/vesilasku/report/device-values-for-report.rlv))
 * produces a csv of readings per month for each meter, total cubic meters,
   and energy required for heating the hot water
   ([example](src/test/resources/fi/kapsi/kosmik/vesilasku/report/expected-report.csv))


## Building

To build the whole thing into a self-contained jar file:

```
sbt assembly
```

To just run tests:

```
sbt test
```