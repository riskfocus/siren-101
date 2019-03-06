EnronSearch
===========
This application was modified based on code found at the below link. It has been updated to support ES 6 and I have added some additional indices.
https://github.com/bcoe/enronsearch

This application was created to upload the Enron email corpus into ES as well as to create additional indices for use in Siren.

EnronSearch is an ElasticSearch index of the 500,000 emails in CMU's Enron corpus.

https://www.cs.cmu.edu/~enron/

EnronSearch:

* provides command-line tools for downloading and indexing the Enron emails.

Installing
----------

You will need to first setup and launch the Siren platform which includes ElasticSearch.
There are a few images available, so for simplicity I used the one with no security enabled and no pre-loaded data.

* Get the image
```bash
docker pull sirensolutions/siren-platform-no-data-no-security:latest
```

* Launch the container

```bash
docker run -d --name siren-enron --mount source=enron-es-vol,destination=/opt/platform/elasticsearch/data --mount source=enron-siren-vol,destination=/opt/platform/siren-investigate/data -d -p 5606:5606 -p 9220:9220 -p 9330:9330 sirensolutions/siren-platform-no-data-no-security:latest
```

To use the full functionality, including the graph browser, you will need a license. You can get a free 30-day trial if you don't have one already.

* Install the license

```bash
curl -k -XPUT -T "<path_to_license>" 'http://localhost:9220/_siren/license' -u sirenadmin:password -H 'Content-Type: application/json'
```

Check that Siren is up and running by navigating to http://localhost:5606/status

Then, to run EnronSearch:
Set the `ES_PORT` and `ES_HOST` environment variables, corresponding to this server.
If you are using the Siren Platform docker images, then the port should be set to 9330.


Once you've done this:

* Install EnronSearch's dependent packages.

```bash
mvn package
```

* Download the Enron email corpus:

```bash
java -cp target/classes:target/dependency/*:./ com.bcoe.enronsearch.Cli --download
```

* Index the corpus:

```bash
java -cp target/classes:target/dependency/*:./ com.bcoe.enronsearch.Cli --index --u
```

Have fun!

Copyright
=========

Copyright (c) 2013 Benjamin Coe. 
Copyright (c) 2019 Bill Wicker, Risk Focus Inc.
 
See LICENSE.txt for further details.
