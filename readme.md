# Overview

java 8, gradle, spring boot, lombok, undertow, hystrix, caliper, ab, Aho-Corasick algorithm


# RESTful API design

There is one endpoint `/find-matches` that accepts `POST` request with `content-type:text/plain;charset=UTF-8` to allow streaming (json frameworks usually require to buffer at least individual values). In fact, endpoint will accept also any other content-type but assumes UTF-8 encoding. Because dictionary doesn't contain any characters like `"`, `\`
or line terminators or `0` char then `content-type:application/json` also works.

The body should containt raw input string using UTF-8 encoding. Reading input will be terminated as soon as all matches are found. Answer is a json array of found matches.

Sample answers:

    ["pain","hip pain"]


When no matches were found:

    []

Some of possible error codes:

* 500: internal algorithm error
* 503: hystrix rejected/terminated request

Beside that standard spring-mvc's error codes are returned.


# Build, test & run

### Build, run unit and integration tests

    ./gradlew build

### Production readiness & tuning

It's impossible to tune application for unknown, never-ever-seen-before machine and unknown traffic distribution. Parameters were set for 4 core machine, 10Mb download speed, in a way to prevent from failing during solution evaluation rather than to ensure optimal throughput.

Application was tuned for constant high load of largest possible requests (~10M).

#### Configuration decisions

As Aho-Corasick algorithm is linear, more time will be spent on reading data rather than doing the actual computation. Therefore number of threads can be much higher thatn number of cores. Application was tuned for constant high load of large tasks so there is no task queue. Task rejection is based on timeout and availability of working threads. Maximum query size is limited by the underlying server's configuration and currently is set to 10Mb.

### Deployment
It's a standard spring boot application with embedded Undertow so it can be run with `./gradlew bootRun` but for convenience there is a run script:

    cd src/runtime
    ./run.sh

that will run already built application inside maven folder structure. It will use configuration files:

    src/runtime/application.yml
    src/runtime/archaius.properties

and will place logs and gc statistics in

    build/logs

Application will listen on port 8080.

If you prefer to run application elsewhere you need to change paths in `run.sh` script.

### Verification with curl

    curl -H "content-type:text/plain;charset=UTF-8" -d "hip pain" http://localhost:8080/find-matches

### Warm-up

Before taking production traffic or benchmarks application should be warmed up. Always send warm-up requests from different machine.

#### Generate files for warmup
Go to `src/runtime` directory and run

    ./make-post-files.sh

Script will create a few files `/tmp/ab-post-X.txt` with requests of different size. Each file contains phrases file (without one entry to avoid early exit) repeated X times.

#### Perform warm-up

Request timeout limit is set way too high because it's easier to ensure that application will work out-of-the box on untested machine and let JIT optimize the correct path.

Later timeout limit can be changed at runtime (`archaius.properties`).

To do actual warm-up put the application under constant load of largest possible queries with concurrency level guaranteeing no errors (min(#threads, #cores)). Later double the concurrency level to optimize also the negative path. For example

    ab -T 'text/plain' -r -p /tmp/ab-post-10.txt -t 600 -c 4 server.com:8080/find-matches

# Algorithm

Classical streamed Aho-Corasick algorithm is used. Matches deduplication is done using hashes. Reading is terminated earlier if all matches are found.

### Complexity

Initial preprocessing time and space:
* O(dictionary size)

Finding matches:
* Time: O(number of matches + input size)
     Dictionary isn't 'evil' so there won't be quadratic number of matches.
* Additional space: O(number of dictionary patterns)


# Benchmarking

#### Tested configuration:

* AMD Phenom II X4 955
* local wifi network
* ubuntu 14.04 64bit
* java version "1.8.0\_66"
    Java(TM) SE Runtime Environment (build 1.8.0\_40-b25)
Java HotSpot(TM) 64-Bit Server VM (build 25.40-b25, mixed mode)
* [ab 2.3](http://httpd.apache.org/docs/2.2/programs/ab.html) (`sudo apt-get install apache2-utils`)
* caliper

### Start-up

Start-up time depends on the dictionary size. Class `DictionaryStats.java` does some simple exploratory analysis of the dictionary and preprocessing time measurement:

* 422 kb so it can be distributed withint application
* ~17k entries, 11 duplicates, no hashcodes collisions between different entries
* avg lenght: 24,2;  max length: 623
* used chars:

       #%&'()*+,-./0123456789:;<=>ABCDEFGHIJKLMNOPRSTUVWXYZ[]^abcdefghijklmnopqrstuvwxyzèéöü

* Preprocessing time: ~330 ms so there is no need for any kind of optimization.


### REST benchmarking

Algorithm is linear and streamed therefore network throughput is the bottleneck. During tests over the network, network is saturated without significant CPU activity. So any kind of stream compression should give significant throughput increase.

Performance tests were done using whole dictionary (without one phrase to avoid early exit) repeated X times.

Always send requests from different. To prepare data go to `src/runtime` and run

    ./make-post-files.sh

To run tests you can use

    src/test/bash/ab-benchmark.sh

just change the server address inside the script and warm-up the server earlier.

##### Results

Used jvm flags:

    -XX:MaxInlineSize=1024 -XX:MaxInlineLevel=20 -XX:InlineSmallCode=10000 -XX:FreqInlineSize=10000


##### Over a network

query size | # requests/sec
---------------|---------
840kb (2 dictionaries)  | 4,36
9.9Mb (24 dictionaries)|  0,58

##### Localhost

To by-pass the network bottleneck and have a rough estimate of the algorithm's performance I did a benhmark on the localhost (still using curl):

query size | # requests/sec
---------------|---------
 840kb (2 dictionaries) | 68,6
 9.9Mb (24 dictionaries) | 5,17

### Algorithm microbenchmarks

To do microbenchamrking with caliper run main method of `AhoCorasickBenchmark.java`. It will run two benchmarks: one with random data (almost no matches) and one with data from dictionary (almost all matches). Possible parameter is `inputSizeKilo`. By default each bechmark is run with query of size 10k, 100k and 10M.

##### Results


query size | execution time (random input) | execution time (dictionary input)
---------------|---------|------
 10k | 0,3 ms   |  0,5 ms
 100k | 2,3 ms | 3,9 ms
 10M | 221 ms | 311 ms



# Monitoring & metrics

Everything provided by spring-boot-actuator and hystrix is available. Among others:

`/metrics`, `/archaius`, `/hystrix.stream`

GC logs location is defined in `src/main/run.sh`. When running with `run.sh` script, it's `build/logs/gcstats.log`. Application logs location is defined in `application.yml`. When running with `run.sh` script, it's  under `build/logs` directory.

# Design decisions

* Specification doesn't solve problem of escaping chars like `'`, `,`,`]` therefore json was used instead, because it has well standarized escaping rules.
* Dictionary is bundled within the application because it's small and there is no requirements to change it in the runtime.
* Lombok is a bit controversial but perfect for small, POC tasks.
* 'plain/text' input is done because json is not well suited for streaming as most frameworks require to buffer at least individual values.


# What's not done

Because of lack of time, laziness, no existing infrastructure, lack of specification and thousands other excuses, some things were not done but sometimes could be useful:

* hystrix command timeout per task size instead of one global value
* remote configuration
* api versioning
* result streaming
* correlation id
* change server's limit on size of request
* runtime documentation on main page (like HATEOAS or swagger)
* better error messages for typical http errors (like unacceptable content-type etc)
* configuration discovery
* GC tuning and benchmarking
* input/output compression, optimization
* reusing json parser buffers
* number of Undertow's thread pool size
* separate modules for algorithm and REST service
