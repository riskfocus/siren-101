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

You will need to have an ElasticSearch server up and running to use EnronSearch.

Set the `ES_PORT` and `ES_HOST` environment variables, corresponding to this server.
If you are using the Siren Platform docker images, then the port should be set to 9330


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
