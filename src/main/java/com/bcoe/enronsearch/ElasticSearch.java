package com.bcoe.enronsearch;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/*
 Wrapper for ElasticSearch, performs
 searching and indexing tasks.
 */
public class ElasticSearch {

	private static String indexName = "enron-messages";
	private static String peopleIndexName = "people";
	private static String groupIndexName = "user-groups";
	private static String mappingType = "_doc";

	private Client client;
	private BulkProcessor bulkProcessor;

	private static final String DEFAULT_ES_HOST = "localhost";
	private static final int DEFAULT_ES_PORT = 9300;

	public ElasticSearch() {
		String envHost = System.getenv("ES_HOST");
		String envPort = System.getenv("ES_PORT");
		String envClusterName = System.getenv("ES_CLUSTER_NAME");

		Settings settings = Settings.builder()
				.put("cluster.name", envClusterName)
				.build();

		try {
			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new TransportAddress(InetAddress.getByName(envHost != null ? envHost : DEFAULT_ES_HOST),
							envPort != null ? Integer.parseInt(envPort): DEFAULT_ES_PORT));

			BulkProcessor.Listener listener = new BulkProcessor.Listener() {
				@Override
				public void beforeBulk(long executionId, BulkRequest request) {
					int numberOfActions = request.numberOfActions();
					System.out.println("Executing bulk [" + executionId + "] with " + numberOfActions + " requests");
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
					if (response.hasFailures()) {
						System.out.println("Bulk [" + executionId + "] executed with failures");
					} else {
//						logger.debug("Bulk [{}] completed in {} milliseconds", executionId, response.getTook().getMillis());
					}
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
					System.err.println("Failed to execute bulk" + failure.getMessage());
				}
			};

			bulkProcessor = BulkProcessor.builder(client, listener)
					.setBulkActions(500)
					.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB))
					.setConcurrentRequests(0)
					.setFlushInterval(TimeValue.timeValueSeconds(10L))
					.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3)).build();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void index() {
		deleteIndex(groupIndexName);
		createIndex(groupIndexName);
		putGroupMapping();
		deleteIndex(peopleIndexName);
		createIndex(peopleIndexName);
		putPeopleMapping();
		deleteIndex(indexName);
		createIndex(indexName);
		putMapping();
	}

	private void deleteIndex(String indexName) {
		try {
			client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void createIndex(String indexName) {
		Settings indexSettings = Settings.builder()
				.put("number_of_shards", 5)
				.put("number_of_replicas", 0)
				.put("analysis.analyzer.default.tokenizer", "uax_url_email")
				.build();

		CreateIndexRequestBuilder createIndexBuilder = client.admin().indices().prepareCreate(indexName);
		createIndexBuilder.setSettings(indexSettings);
		createIndexBuilder.execute().actionGet();

		waitForYellow(indexName);
	}

	private void waitForYellow(String indexName) {
		ClusterHealthRequestBuilder healthRequest = client.admin().cluster().prepareHealth();

		healthRequest.setIndices(indexName);
		healthRequest.setWaitForYellowStatus();

		ClusterHealthResponse healthResponse = healthRequest.execute().actionGet();
	}

	private void putMapping() {
		try {
			
			XContentBuilder mapping = XContentFactory.jsonBuilder()
					.startObject()
						.startObject("properties")
							.startObject("to")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("from")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("subject")
								.field("type", "text")
							.endObject()
							.startObject("body")
								.field("type", "text")
							.endObject()
							.startObject("date")
								.field("type", "date")
//								.field("format", "EEE, d MMM yyyy HH:mm:ss Z (z)")
							.endObject()
							.startObject("cc")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("bcc")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("recipients")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("xcc")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("xbcc")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("userid")
								.field("type", "text")
								.startObject("fields")
									.startObject("raw")
										.field("type", "keyword")
									.endObject()
								.endObject()
							.endObject()
							.startObject("origin")
								.field("type", "text")
							.endObject()
						.endObject()
					.endObject();

			client.admin().indices().preparePutMapping(indexName)
					.setType(mappingType).setSource(mapping).execute()
					.actionGet();

			flush(indexName);
		} catch (IOException e) {
			System.out.println("Failed to put mapping: " + e);
		}
	}

	private void putPeopleMapping() {
		try {

			XContentBuilder mapping = XContentFactory.jsonBuilder()
					.startObject()
					  .startObject("properties")
					    .startObject("userId")
					      .field("type", "text")
							.startObject("fields")
								.startObject("raw")
									.field("type", "keyword")
								.endObject()
							.endObject()
					    .endObject()
					    .startObject("groups")
  					      .field("type", "text")
						.startObject("fields")
						.startObject("raw")
						.field("type", "keyword")
						.endObject()
						.endObject()
					    .endObject()
					  .endObject()
					.endObject();
			client.admin().indices().preparePutMapping(peopleIndexName)
					.setType(mappingType).setSource(mapping).execute()
					.actionGet();

			flush(peopleIndexName);
		} catch (IOException e) {
			System.out.println("Failed to put mapping: " + e);
		}
	}

	private void putGroupMapping() {
		try {

			XContentBuilder mapping = XContentFactory.jsonBuilder()
					.startObject()
					.startObject("properties")
					.startObject("groupId")
					.field("type", "text")
					.startObject("fields")
					.startObject("raw")
					.field("type", "keyword")
					.endObject()
					.endObject()
					.endObject()
					.startObject("groupName")
					.field("type", "text")
					.endObject()
					.endObject()
					.endObject();
//			System.out.println(Strings.toString(mapping));
			client.admin().indices().preparePutMapping(groupIndexName)
					.setType(mappingType).setSource(mapping).execute()
					.actionGet();

			flush(groupIndexName);
		} catch (IOException e) {
			System.out.println("Failed to put mapping: " + e);
		}
	}

	private void flush(String indexName) {
		client.admin().indices().flush(new FlushRequest(indexName)).actionGet();
	}

	public void indexMessage(Message message, boolean upload) {
		try {
			DateTimeParser[] parsers = {
					DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss Z (z)").getParser(),
					DateTimeFormat.forPattern("EEEE, MMMM d, yyyy hh:mm a").getParser()
			};
			DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
			
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
			DateTime dateTime = formatter.parseDateTime(message.getDateString());
			XContentBuilder document = XContentFactory.jsonBuilder()
					.startObject()
					.field("to", message.getTo())
					.field("from", message.getFrom())
					.field("subject", message.getSubject())
					.field("body", message.getBody())
					.field("cc", message.getCc())
					.field("bcc", message.getBcc())
					.field("date", fmt.print(dateTime))
					.field("recipients", message.getRecipients())
					.field("origin", message.getxOrigin())
					.endObject();

			if (upload) {
				IndexRequest request = client.prepareIndex(indexName, mappingType, message.getId()).setSource(document).request();
				bulkProcessor.add(request);
			} else {
				System.out.println(Strings.toString(document));
			}
		} catch (IOException e) {
			System.out.println(e);
		} catch(Exception e) {
			System.out.println("Error parsing date " + message.getDateString()+" for message with subject " + message.getSubject());
			
		}
	}

	public void indexPeople(Collection<Person> people, boolean upload) {
		try {
			XContentBuilder document;
			for (Person person : people) {
				document = XContentFactory.jsonBuilder()
						.startObject()
						.field("userId", person.getId())
						.field("groups", person.getGroups())
						.endObject();

				if (upload) {
					bulkProcessor.add(client.prepareIndex(peopleIndexName, mappingType, person.getId()).setSource(document).request());
				} else {
					System.out.println(Strings.toString(document));
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void indexGroups(boolean upload) {
		try {
			XContentBuilder document;

			String[] groups = new String[] {"A", "B", "C", "D", "E", "F"};

			for (String group : groups) {
				document = XContentFactory.jsonBuilder()
						.startObject()
						.field("groupId", group)
						.field("groupName", "Group " + group)
						.endObject();
				if (upload) {
					bulkProcessor.add(client.prepareIndex(groupIndexName, mappingType, group).setSource(document).request());
				} else {
					System.out.println(Strings.toString(document));
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void cleanup() {
		try {
			if (bulkProcessor.awaitClose(30L, TimeUnit.SECONDS)) {
				bulkProcessor.close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bulkProcessor.close();
		}
		client.close();
	}
}
