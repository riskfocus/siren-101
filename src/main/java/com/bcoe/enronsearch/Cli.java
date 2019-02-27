package com.bcoe.enronsearch;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Provides a command line interface for
indexing documents and downloading the
Enron corpus.
*/
public class Cli {

    public static void main( String[] args ) {

      CommandLineParser parser = new BasicParser();

      Options options = new Options();

      options.addOption( "d", "download", false, "download enron dataset." );
      options.addOption( "i", "index", false, "reindex the enron dataset." );
      options.addOption( "u", "upload", false, "Upload the new index" );
      options.addOption( "h", "help", false, "display help information." );

      try {
          
          CommandLine line = parser.parse( options, args );

          // Print available options.
          if ( line.hasOption("h") ) {

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "enronsearch", options );

          }

          // Download the enron email corpus.
          if( line.hasOption( "d" ) ) {
            
            Downloader downloader = new Downloader();
            System.out.println( "downloading enron mail dataset.");
            downloader.download();
            System.out.println( "download complete." );

          } else if ( line.hasOption( "i" ) ) {
              boolean upload = line.hasOption("u");

              // Reindex the enron corpus.
              System.out.println("index documents in enron dataset");
              DatasetIterator datasetIterator = new DatasetIterator();
              Map<String, Person> people = new HashMap<>();

              ElasticSearch es = new ElasticSearch();

              if (upload){
                  es.index();
              }
              es.indexGroups(upload);

              Message message;

              while ( (message = datasetIterator.nextMessage()) != null ) {
                  checkPeople(message, message.getFrom(), people);
                  checkPeople(message, message.getRecipients(), people);

                  es.indexMessage(message, upload);
              }

              es.indexPeople(people.values(), upload);
              if (upload) {
                  es.cleanup();
              }
          }
      } catch( ParseException exp ) {
          System.out.println( "Unexpected exception:" + exp.getMessage() );
      }
    }

    private static void checkPeople(Message message, String[] ids, Map<String, Person> people) {
        if (ids == null)
            return;

        for (String id : ids) {
            checkPeople(message, id, people);
        }
    }

    private static void checkPeople(Message message, String id, Map<String, Person> people) {
        if (!people.containsKey(id)) {
            people.put(id, getPerson(message, id));
        }
    }

    private static Person getPerson(Message message, String id) {
        Person p  = new Person(id);
        p.setGroups(assignGroups());
        return p;
    }

    private static String[] assignGroups() {
        List<String> groups = new ArrayList<>(6);
        groups.add("A");
        groups.add("B");
        groups.add("C");
        groups.add("D");
        groups.add("E");
        groups.add("F");
        Collections.shuffle(groups);

        return groups.subList(0, 1).toArray(new String[1]);

    }
}
