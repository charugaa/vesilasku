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


## Usage

After you have build the jar (see below), you can get usage instructions with:
```
java -jar target/scala-2.12/vesilasku-assembly-0.1.0-SNAPSHOT.jar --help
```

To actually produce a report, you could do, for example:
```
java -jar target/scala-2.12/vesilasku-assembly-0.1.0-SNAPSHOT.jar \
  -a <apartment meters csv> \
  -d <sontex devices values csv (rlv)> \
  -c 3 \    # number of months in report
  -m 6 \    # last month in report
  -y 2017 \ # year
  -w 58     # kilowatt hours per cubic meter of hot water
```

## Building

To build the whole thing into a self-contained jar file:

```
sbt assembly
```

To just run tests:

```
sbt test
```