package com.loserico.search;

import com.loserico.common.lang.utils.ReflectionUtils;
import com.loserico.search.document.DocumentRequests;
import com.loserico.search.enums.Analyzer;
import com.loserico.search.enums.Dynamic;
import com.loserico.search.enums.FieldType;
import com.loserico.search.support.FieldDef;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.loserico.json.jackson.JacksonUtils.toJson;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Copyright: (C), 2020-11-21 10:03
 * <p>
 * <p>
 * Company: Information & Data Security Solutions Co., Ltd.
 *
 * @author Rico Yu ricoyu520@gmail.com
 * @version 1.0
 */
public class ESTest {
	
	private static RestHighLevelClient client = null;
	
	private static ElasticsearchOperations operations = new ElasticsearchOperations();
	
	@BeforeClass
	@SneakyThrows
	public static void init() {
		RestClientBuilder restClientBuilder;
		client = new RestHighLevelClient(RestClient.builder(
				new HttpHost("192.168.100.104", 9200, "http")));
		ReflectionUtils.setField("client", operations, client);
	}
	
	@AfterClass
	@SneakyThrows
	public static void close() {
		client.close();
	}
	
	@Test
	public void testCreateIndex() {
		boolean created = operations.createIndex("dynamic_mapping_test");
		System.out.println(created);
	}
	
	/**
	 * 查看索引相关信息
	 * GET kibana_sample_data_ecommerce
	 */
	@Test
	@SneakyThrows
	public void testGetIndex() {
		//The index whose information we want to retrieve
		GetIndexRequest request = new GetIndexRequest("kibana_sample_data_ecommerce");
		//If true, defaults will be returned for settings not explicitly set on the index
		request.includeDefaults(true);
		request.indicesOptions(IndicesOptions.lenientExpandOpen());
		//同步调用
		GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
		
		//Retrieve a Map of different types to MappingMetadata for index.
		MappingMetaData indexMapping = response.getMappings().get("kibana_sample_data_ecommerce");
		Map<String, Object> indexTypeMappings = indexMapping.getSourceAsMap();
		List<AliasMetaData> indexAliases = response.getAliases().get("kibana_sample_data_ecommerce");
		Settings settings = response.getSettings().get("kibana_sample_data_ecommerce");
		CompressedXContent mappings = indexMapping.source();
		
		JSONObject getIndexObject = new JSONObject();
		System.out.println(indexTypeMappings.toString());
		System.out.println(toJson(mappings));
		System.out.println(toJson(indexAliases));
		System.out.println(settings.toString());
	}
	
	@Test
	public void testExistsIndex() {
		assertTrue(operations.existsIndex("rico"));
	}
	
	@Test
	public void testListIndices() {
		operations.listIndices().forEach(System.out::println);
	}
	
	@Test
	public void testDeleteIndex() {
		boolean deleted = operations.deleteIndex("dynamic_mapping_test");
		System.out.println(deleted);
	}
	
	@Test
	public void testDeleteDoc() {
		DocWriteResponse.Result result = operations.delete("test", "1");
		System.out.println(result);
	}
	
	/**
	 * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.6/java-rest-high-count.html
	 * <p>
	 * 查看索引文档总数
	 * GET kibana_sample_data_ecommerce/_count
	 */
	@Test
	@SneakyThrows
	public void testIndexDocCount() {
		CountRequest countRequest = new CountRequest("kibana_sample_data_ecommerce");
		CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
		long count = countResponse.getCount();
		System.out.println(count);
		int failedShards = countResponse.getFailedShards();
		System.out.println(failedShards);
	}
	
	/**
	 * 不指定ID创建文档
	 */
	@Test
	public void xtestCreateDocument() {
		String id = operations.index("rico", "{\"name\": \"三少爷\"}");
		System.out.println(id);
	}
	
	/**
	 * 不指定ID创建文档
	 */
	@Test
	public void testCreateDocumentWithId() {
		DocumentRequests documentRequests = new DocumentRequests(client);
		String id = operations.indexIfNotExists("rico", "1", "{\"name\": \"三少爷\"}");
		System.out.println(id);
	}
	
	/**
	 * 不指定ID创建文档
	 */
	@Test
	public void testSaveDocumentWithId() {
		String id = operations.index("rico", "1", "{\"name\": \"三少爷1\"}");
		System.out.println(id);
	}
	
	@Test
	public void testUpdateDoc() {
		DocWriteResponse.Result result = operations.update("rico", "ShNSknYBCTOc63k_prHg", "{\"age\": 39}");
		System.out.println(result);
	}
	
	@Test
	public void testUpsertDoc() {
		DocWriteResponse.Result result = operations.upsert("rico", "4", "{\"age\": 39}");
		System.out.println(result);
	}
	
	@Test
	public void testDocExists() {
		boolean exists = operations.exists("rico", "1");
		assertTrue(exists);
	}
	
	@Test
	public void testBulkCreate() {
		int count = operations.index("rico", asList("{\"age\": 39}", "{\"age\": 39}", "{\"age\": 39}"));
		System.out.println(count);
	}
	
	/**
	 * https://www.javacodegeeks.com/2020/01/elasticsearch-sql.html
	 */
	@Test
	public void testSqlQuery() {
		
	}
	
	@Test
	public void testGet() {
		Person person = operations.get("my_index", "2", Person.class);
		assertEquals(person.getUser(), "mike");
	}
	
	@Test
	public void testMGet() {
		List<Person> persons = operations.mget().add("my_index", "1")
				.add("my_index", "2")
				.resultType(Person.class)
				.request();
		persons.forEach(System.out::println);
	}
	
	@Test
	public void testGetMapping() {
		Object dynamic_mapping_test = operations.getMapping("dynamic_mapping_test");
		System.out.println(toJson(dynamic_mapping_test));
	}
	
	@Test
	public void testPutMapping() {
		//operations.putMapping("dynamic_mapping_test");
		operations.putMapping("dynamic_mapping_test")
				.copy("dynamic_mapping_test")
				.dynamic(Dynamic.FALSE)
				.field("firstName", FieldType.KEYWORD, false)
				.field(FieldDef.builder("fullName", FieldType.TEXT)
						.analyzer(Analyzer.ICU)
						.index(true)
						.build())
				.field("birthday", FieldType.DATE)
				.field(FieldDef.builder("workDate", FieldType.DATE)
						.format("yyyy-MM-dd")
						.build())
				.execute();
	}
	
	@Test
	public void testSearchAll() {
		Object response = operations.searchAll("kibana_sample_data_ecommerce");
		System.out.println(response);
	}
	
	@Data
	private static class Person {
		private String user;
		private String comment;
	}
}
