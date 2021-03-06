package com.loserico.search;

import com.loserico.search.annotation.DocId;
import com.loserico.search.builder.MappingBuilder;
import com.loserico.search.enums.Analyzer;
import com.loserico.search.enums.ContextType;
import com.loserico.search.enums.Dynamic;
import com.loserico.search.enums.FieldType;
import com.loserico.search.pojo.Movie;
import com.loserico.search.support.BulkResult;
import com.loserico.search.support.FieldDef;
import com.loserico.search.support.UpdateResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.loserico.json.jackson.JacksonUtils.toJson;
import static com.loserico.search.enums.FieldType.COMPLETION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.suggest.SortBy.FREQUENCY;
import static org.elasticsearch.search.suggest.term.TermSuggestionBuilder.StringDistanceImpl.INTERNAL;
import static org.junit.Assert.*;

/**
 * <p>
 * Copyright: (C), 2021-01-01 9:06
 * <p>
 * <p>
 * Company: Information & Data Security Solutions Co., Ltd.
 *
 * @author Rico Yu ricoyu520@gmail.com
 * @version 1.0
 */
public class ElasticUtilsTest {
	
	@BeforeClass
	public static void testInitialize() {
		Class<ElasticUtils> elasticUtilsClass = ElasticUtils.class;
		assertThat(ElasticUtils.client != null);
	}
	
	@Test
	public void testDeleteIndex() {
		boolean created = ElasticUtils.deleteIndex("boduo");
		System.out.println(created);
	}
	
	@Test
	public void testCreateIndex() {
		boolean created = ElasticUtils.createIndex("boduo")
				.mapping(MappingBuilder.newInstance()
						.dynamic(Dynamic.TRUE)
						.field("name", FieldType.TEXT)
						.field("income", FieldType.LONG, false)
						.field(FieldDef.builder("carrer", FieldType.TEXT)
								.index(true)
								.analyzer(Analyzer.IK_MAX_WORD)
								.searchAnalyzer(Analyzer.IK_SMART)
								.build()))
				.create();
		System.out.println(created);
	}
	
	@Test
	public void testExistsIndex() {
		assertTrue(ElasticUtils.existsIndex("dynamic_mapping_test"));
	}
	
	@Test
	public void testListAllIndices() {
		List<String> indices = ElasticUtils.listIndices();
		indices.forEach(System.out::println);
	}
	
	@Test
	public void testCreateDoc() {
		String id = ElasticUtils.index("rico", "{\"name\": \"三少爷\"}");
		System.out.println(id);
	}
	
	@Test
	public void testCreateDocWithId() {
		String id = ElasticUtils.index("rico", "{\"name\": \"三少爷\"}", "1");
		System.out.println(id);
	}
	
	@Test
	public void testCreateDocObjectType() {
		Person person = new Person();
		person.setUser("三少爷");
		person.setComment("牛仔");
		String id = ElasticUtils.index("rico", person);
		System.out.println(id);
	}
	
	@Test
	public void testCreateDocObjectTypeAndId() {
		Person person = new Person();
		person.setUser("三少爷");
		person.setComment("牛仔");
		String id = ElasticUtils.index("rico", person);
		System.out.println(id);
	}
	
	@Test
	public void testCreateDocObjectTypeAutoId() {
		Person person = new Person();
		person.setId(1);
		person.setUser("三少爷");
		person.setComment("牛仔");
		String id = ElasticUtils.index("rico", person);
		System.out.println(id);
	}
	
	@Test
	public void testBulkCreateDoc() {
		String[] docs = new String[]{"{\"name\": \"三少爷\"}", "{\"name\": \"二少爷\"}", "{\"name\": \"大少爷\"}"};
		BulkResult bulkResult = ElasticUtils.bulkIndex("rico", docs);
		System.out.println(toJson(bulkResult));
	}
	
	@Test
	public void testBulkCreate() {
		List<Person> persons = new ArrayList<>();
		persons.add(new Person(1, "Json", "this is jason born"));
		persons.add(new Person(2, "Icon Man", "this is Stark"));
		persons.add(new Person(3, "Sea King", "this is 海王"));
		
		BulkResult bulkResult = ElasticUtils.bulkIndex("rico", persons);
		System.out.println(toJson(bulkResult));
	}
	
	@Test
	public void testBulkCreateProduct() {
		List<Product> products = asList(new Product("1", "XHDK-A-1293-#fJ3", "iPhone"),
				new Product("2", "KDKE-B-9947-#kL5", "iPad"),
				new Product("3", "JODL-X-1937-#pV7", "MBP"));
		BulkResult bulkResult = ElasticUtils.bulkIndex("products", products);
		System.out.println(toJson(bulkResult));
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Product {
		
		@DocId
		private String id;
		
		private String productId;
		
		private String desc;
		
		
	}
	
	@Test
	public void testGetById() {
		User user = ElasticUtils.get("rico", "ShNSknYBCTOc63k_prHg", User.class);
		System.out.println(user);
	}
	
	@Test
	public void testMget() {
		List<String> results = ElasticUtils.mget()
				.add("rico", "wdL8N3YBfUXxQhjl2Pc2")
				.add("rico", "vNLwN3YBfUXxQhjlgvfr")
				.add("users", asList("1", "2"))
				.request();
		results.forEach(System.out::println);
	}
	
	@Test
	public void testUpdate() {
		/*DocWriteResponse.Result result = ElasticUtils.update("users", "1", "{\n" +
				"  \"firstName\": \"Rico\",\n" +
				"  \"lastName\": \"Johnson\"\n" +
				"}");*/
		UpdateResult updateResult = ElasticUtils.update("users", "1", "{\"nickname\": \"三少爷\"}");
		System.out.println(toJson(updateResult));
	}
	
	@Test
	public void testUpsert() {
		UpdateResult updateResult = ElasticUtils.upsert("users", "3", "{\n" +
				"  \"firstName\": \"Rico\",\n" +
				"  \"lastName\": \"Johnson\"\n" +
				"}");
		System.out.println(toJson(updateResult));
	}
	
	@Test
	public void testUpsert2() {
		UpdateResult updateResult = ElasticUtils.upsert("users", "3", "{\"nickname\": \"三少爷\"}");
		System.out.println(toJson(updateResult));
	}
	
	@Test
	public void testDelteDoc() {
		boolean deleted = ElasticUtils.delete("rico", "UWHGu3YBDs-1X2rMuqw4");
		System.out.println(deleted);
	}
	
	@Test
	public void testDeleteBy() {
		long deleted = ElasticUtils.deleteBy("rico", "user", "Sea King");
		System.out.println(deleted);
	}
	
	@Test
	public void testExists() {
		boolean exists = ElasticUtils.exists("users", "3");
		System.out.println(exists);
	}
	
	@Test
	public void testGetMapping() {
		Object mapping = ElasticUtils.getMapping("boduo");
		System.out.println(toJson(mapping));
	}
	
	@Test
	public void testGetFieldMapping() {
		Map<String, Map<String, Object>> result = ElasticUtils.getMapping("boduo", "carrer", "fans", "income");
		System.out.println(toJson(result));
	}
	
	@Test
	public void testPutMapping() {
		boolean acknowledged = ElasticUtils.putMapping("rico", MappingBuilder.newInstance()
				.copy("movies")
				.dynamic(Dynamic.TRUE)
				.field(FieldDef.builder("title", FieldType.KEYWORD)
						.index(true)
						.build()));
		System.out.println(acknowledged);
	}
	
	@Test
	public void testPutMappingWithDeleteFieldDef() {
		boolean acknowledged = ElasticUtils.putMapping("rico", MappingBuilder.newInstance()
				.copy("movies")
				.dynamic(Dynamic.TRUE)
				.field(FieldDef.builder("title", FieldType.KEYWORD)
						.index(true)
						.build())
				.delete("user", "genre"));
		System.out.println(acknowledged);
	}
	
	@Test
	public void testPutMappingAddNewFields() {
		ElasticUtils.putMapping("boduo", MappingBuilder.newInstance()
				.field("fans", FieldType.TEXT));
	}
	
	@Test
	public void testPutMappingWithChildField() {
		/*boolean created = ElasticUtils.createIndex("titles").create();
		boolean acknowledged = ElasticUtils.putMapping("titles", MappingBuilder.newInstance()
				.field(FieldDef.builder("title", FieldType.TEXT)
						.fields(FieldDef.builder("std", FieldType.TEXT)
								.analyzer(Analyzer.STANDARD)
								.build())
						.build()));
		System.out.println(acknowledged);*/
		
		ElasticUtils.deleteIndex("titles");
		boolean acknowledged = ElasticUtils.createIndex("titles")
				.mapping(MappingBuilder.newInstance()
						.field(FieldDef.builder("title", FieldType.TEXT)
								.fields(FieldDef.builder("std", FieldType.TEXT)
										.analyzer(Analyzer.STANDARD)
										.build())
								.build()))
				.create();
	}
	
	@Test
	public void testPutIndexTemplate() {
		boolean created = ElasticUtils.putIndexTemplate("demo-index-template")
				.order(0)
				.patterns("test*")
				.version(0)
				.settings(Settings.builder()
						.put("number_of_shards", 1)
						.put("number_of_replicas", 1)
						.build())
				.mappings(MappingBuilder.newInstance()
						.dynamic(Dynamic.TRUE)
						.field("username", FieldType.KEYWORD)
						.field("read_books", FieldType.TEXT)
						.field(FieldDef.builder("hobbys", FieldType.TEXT).index(true).build()))
				.execute();
		System.out.println(created);
	}
	
	@Test
	public void testGetIndexTemplate() {
		IndexTemplateMetaData indexTemplateMetaData = ElasticUtils.getIndexTemplate("demo-index-template");
		System.out.println(toJson(indexTemplateMetaData));
	}
	
	@Test
	public void testCreateIndexWithIndexTemplate() {
		boolean created = ElasticUtils.createIndex("testINdex").create();
	}
	
	@Test
	public void testDeleteIndexTemplate() {
		boolean deleted = ElasticUtils.deleteIndexTemplate("demo-index-template");
		System.out.println(deleted);
	}
	
	@Data
	public static class User {
		private String name;
		private int age;
	}
	
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Person {
		
		@DocId
		private Integer id;
		private String user;
		private String comment;
	}
	
	@Test
	public void testBoolQuery() {
		List<String> products = ElasticUtils.query("products")
				.queryBuilder(boolQuery()
						.must(QueryBuilders.termQuery("price", 30))
						.filter(QueryBuilders.termQuery("avaliable", true))
						.mustNot(QueryBuilders.rangeQuery("price").lte(10))
						.should(QueryBuilders.termQuery("productID.keyword", "JODL-X-1937-#pV7"))
						.should(QueryBuilders.termQuery("productID.keyword", "XHDK-A-1293-#fJ3"))
						.minimumShouldMatch(1)
				)
				.queryForList();
		
		products.forEach(System.out::println);
		List<String> blogs = ElasticUtils.query("blogs")
				.queryBuilder(boolQuery()
						.should(matchQuery("title", "apple,ipad").boost(1.1f))
						.should(matchQuery("Content", "apple,ipad").boost(2f)))
				.queryForList();
		
		blogs.forEach(System.out::println);
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class News {
		
		@DocId
		private String id;
		
		private String content;
	}
	
	@Test
	public void testBoolQuery2() {
		/*ElasticUtils.deleteIndex("news");
		
		News news1 = new News("1", "Apple Mac");
		News news2 = new News("2", "Apple iPad");
		News news3 = new News("3", "Apple employee like Apple Pie and Apple Juice");
		BulkResult bulkResult = ElasticUtils.bulkIndex("news", asList(news1, news2, news3));
		System.out.println(bulkResult);*/
		
		BoolQueryBuilder boolQueryBuilder = boolQuery()
				.must(matchQuery("content", "apple"))
				.mustNot(matchQuery("content", "pie"));
		List<String> news = ElasticUtils.query("news")
				.queryBuilder(boolQueryBuilder)
				.queryForList();
		news.forEach(System.out::println);
		
	}
	
	@Test
	public void testBoolBoostingQuery() {
		BoostingQueryBuilder queryBuilder = boostingQuery(matchQuery("content", "apple"), matchQuery("content", "pie"));
		queryBuilder.negativeBoost(0.5f);
		List<String> news = ElasticUtils.query("news")
				.queryBuilder(queryBuilder)
				.queryForList();
		news.forEach(System.out::println);
	}
	
	@Test
	public void testBoost() {
		MatchQueryBuilder titleQueryBuilder = matchQuery("title", "apple,ipad").boost(4f);
		MatchQueryBuilder contentQueryBuilder = matchQuery("content", "apple,ipad").boost(1f);
		List<Object> blogs = ElasticUtils.query("blogs")
				.queryBuilder(boolQuery().should(titleQueryBuilder).should(contentQueryBuilder))
				.queryForList();
		blogs.forEach(System.out::println);
	}
	
	@Test
	public void testDisjunctionQuery() {
		DisMaxQueryBuilder queryBuilder = disMaxQuery()
				.add(matchQuery("title", "Brown fox"))
				.add(matchQuery("body", "Brown fox"));
		queryBuilder.tieBreaker(0f);
		List<String> blogs = ElasticUtils.query("blogs")
				.queryBuilder(queryBuilder)
				.queryForList();
		blogs.forEach(System.out::println);
	}
	
	@Test
	public void testMultiMatchQuery() {
		MultiMatchQueryBuilder multiMatchQueryBuilder = multiMatchQuery("Quick pets", "title", "body")
				.tieBreaker(0.2f)
				.type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
				.minimumShouldMatch("20%");
		List<Object> blogs = ElasticUtils.query("blogs")
				.queryBuilder(multiMatchQueryBuilder)
				.queryForList();
		blogs.forEach(System.out::println);
	}
	
	@Test
	public void testQueryStringQuery() {
		List<Object> objects = ElasticUtils.query("users")
				.queryBuilder(queryStringQuery("Ruan AND Yiming").field("name"))
				.queryForList();
		objects.forEach(System.out::println);
	}
	
	@Test
	public void testQueryStringQuery2() {
		//QueryStringQueryBuilder queryBuilder = queryStringQuery("name:Ruan AND Yiming");
		QueryStringQueryBuilder queryBuilder = queryStringQuery("-Ruan +Yiming").field("name");
		//QueryStringQueryBuilder queryBuilder = queryStringQuery("NOT Ruan Yiming").field("name");
		List<Object> users = ElasticUtils.query("users")
				.queryBuilder(queryBuilder)
				.queryForList();
		users.forEach(System.out::println);
	}
	
	@Test
	public void testCreateDelteAlias() {
		boolean indexDeleted = ElasticUtils.deleteIndex("test-2021-01-28");
		System.out.println("Index deleted: " + indexDeleted);
		boolean indexCreated = ElasticUtils.createIndex("test-2021-01-28").create();
		System.out.println("Index created: " + indexCreated);
		assertTrue(indexCreated);
		boolean created = ElasticUtils.createIndexAlias("test-2021-01-28", "test");
		assertTrue(created);
		System.out.println("Alias created: " + created);
		boolean deleted = ElasticUtils.deleteIndexAlias("test-2021-01-28", "test");
		System.out.println("Alias deleted: " + deleted);
		assertTrue(deleted);
	}
	
	@Test
	public void test() {
		System.out.println(new Date(1611763200000L));
		System.out.println(new Date(1614527999000L));
	}
	
	@Test
	public void testCount() {
		Date begin = new Date(1611763200000L); //Thu Jan 28 00:00:00 CST 2021
		Date end = new Date(1614527999000L); //Sun Feb 28 23:59:59 CST 2021
		String[] indices =
				new String[]{"event_2021_02_08", "event_2021_02_09", "event_2021_02_04", "event_2021_02_26", "event_2021_02_05", "event_2021_02_27", "event_2021_02_06", "event_2021_02_28", "event_2021_02_07", "event_2021_02_22", "event_2021_02_01", "event_2021_02_23", "event_2021_02_02", "event_2021_02_24", "event_2021_02_03", "event_2021_02_25", "event_2021_02_10", "event_2021_01_29", "event_2021_02_19", "event_2021_02_15", "event_2021_02_16", "event_2021_02_17", "event_2021_02_18", "event_2021_01_28", "event_2021_02_11", "event_2021_02_12", "event_2021_02_13", "event_2021_02_14", "event_2021_02_20", "event_2021_02_21", "event_2021_01_31", "event_2021_01_30"};
		RangeQueryBuilder builder = rangeQuery("datetime")
				.gte(begin)
				.lte(end);
		Long totalCount = ElasticUtils.query(indices)
				.queryBuilder(builder)
				.fetchSource(false)
				.getCount();
		System.out.println(totalCount);
	}
	
	@Test
	public void testSuggest() {
		TermSuggestionBuilder suggestionBuilder =
				SuggestBuilders.termSuggestion("body")
						.suggestMode(TermSuggestionBuilder.SuggestMode.ALWAYS)
						.text("luce")
						.prefixLength(1)
						.stringDistance(INTERNAL)
						.sort(FREQUENCY);
		
		Set<String> suggesters = ElasticUtils.suggest("articles")
				.name("term-suggestion")
				.suggestionBuilder(suggestionBuilder)
				.suggest();
		
		suggesters.forEach(System.out::println);
	}
	
	@Test
	public void testSuggest2() {
		TermSuggestionBuilder suggestionBuilder =
				SuggestBuilders.termSuggestion("body")
						.suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR)
						.text("lucen hocks")
						//.prefixLength(0)
						.stringDistance(INTERNAL)
						.sort(FREQUENCY);
		
		Set<String> suggesters = ElasticUtils.suggest("articles")
				.name("term-suggestion")
				.suggestionBuilder(suggestionBuilder)
				.suggest();
		
		suggesters.forEach(System.out::println);
		
		ElasticUtils.termSuggest("lucen rock", "body", "articles")
				.forEach(System.out::println);
	}
	
	@Test
	public void testPhraseSuggester() {
		PhraseSuggestionBuilder suggestionBuilder = SuggestBuilders.phraseSuggestion("body")
				.text("lucne and elasticsear rock")
				.maxErrors(2f)
				.confidence(0)
				.highlight("<em>", "</em>");
		Set<String> suggests = ElasticUtils.suggest("articles")
				.suggestionBuilder(suggestionBuilder)
				.name("phrase-suggestion")
				.suggest();
		
		suggests.forEach(System.out::println);
	}
	
	@Test
	public void testCompletionSuggestion() {
		ElasticUtils.deleteIndex("articles");
		ElasticUtils.createIndex("articles")
				.mapping(MappingBuilder.newInstance()
						.field(FieldDef.builder("title_completion", COMPLETION).build()))
				.create();
		BulkResult bulkResult = ElasticUtils.bulkIndex("articles",
				"{\"title_completion\": \"lucene is very cool\"}",
				"{\"title_completion\": \"Elasticsearch builds on top of Lucene\"}",
				"{\"title_completion\": \"Elasticsearch rocks\"}",
				"{\"title_completion\": \"elastic is the company behind ELK stack\"}",
				"{\"title_completion\": \"TLK stack rocks\"}");
		CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("title_completion").prefix("e");
		Set<String> suggests = ElasticUtils.suggest("articles")
				.suggestionBuilder(suggestionBuilder)
				.name("article_suggester")
				.suggest();
		
		suggests.forEach(System.out::println);
		System.out.println("-----------------");
		
		suggests = ElasticUtils.completionSuggest("e", "title_completion", "articles");
		suggests.forEach(System.out::println);
		
	}
	
	@SneakyThrows
	@Test
	public void testContextCompletion() {
		ElasticUtils.deleteIndex("comments");
		
		FieldDef fieldDef = FieldDef.builder("comment_autocomplete", COMPLETION)
				.addContext(ContextType.CATEGORY, "comment_category")
				.build();
		
		ElasticUtils.createIndex("comments")
				.mapping(MappingBuilder.newInstance()
						.field(fieldDef))
				.create();
		
		ElasticUtils.index("comments", "{\"comment\": \"I love the star war movies\", \"comment_autocomplete\": {\"input\": [\"star wars\"], \"contexts\": {\"comment_category\": \"movies\"} } }");
		ElasticUtils.index("comments", "{\"comment\": \"Where can Ifind a Starbucks\", \"comment_autocomplete\": {\"input\": [\"starbucks\"], \"contexts\": {\"comment_category\": \"coffee\"} } }");
		
		TimeUnit.SECONDS.sleep(1);
		
		Map<String, List<? extends ToXContent>> contexts = Collections.singletonMap("comment_category",
				asList(CategoryQueryContext.builder()
						.setCategory("coffee")
						.build()));
		CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("comment_autocomplete")
				.prefix("sta")
				.contexts(contexts);
		Set<String> suggests = ElasticUtils.suggest("comments")
				.suggestionBuilder(completionSuggestionBuilder)
				.name("contextSuggest")
				.suggest();
		suggests.forEach(System.out::println);
	}
	
	@Test
	public void testContextSuggestion() {
		Set<String> suggests = ElasticUtils.contextSuggest("comments")
				.name("contextSuggestName")
				.category("movies")
				.categoryName("comment_category")
				.field("comment_autocomplete")
				.prefix("sta")
				.suggest();
		
		suggests.forEach(System.out::println);
	}
	
	@Test
	public void testTermQuery() {
		/*
		 * ES 不会对你输入的条件做任何的分词处理
		 * 但是文档在被加入索引的时候, desc字段又是被分词了的, 大写字母转成了小写
		 * 所以这里term query查desc字段的话必须用小写的iphone
		 */
		List<Object> objects = ElasticUtils.query("products")
				.queryBuilder(termQuery("desc", "iphone"))
				.queryForList();
		objects.forEach(System.out::println);
		
		/*
		 * 如果非要精确匹配大小写, 那么可以term query查desc.keyword
		 * 这是ES的一个多字段特定, 默认会为text类型的字段创建一个keyword类型的子字段
		 */
		objects = ElasticUtils.query("products")
				.queryBuilder(termQuery("desc.keyword", "iPhone"))
				.queryForList();
		objects.forEach(System.out::println);
	}
	
	@Test
	public void testConstantScoreQuery() {
		List<Object> products = ElasticUtils.constantScoreQuery("products")
				.queryBuilder(termQuery("productID.keyword", "JODL-X-1937-#pV7"))
				.queryForList();
		products.forEach(System.out::println);
	}
	
	@Test
	public void testStructuredQuery() {
		RangeQueryBuilder rangeQueryBuilder = rangeQuery("date").gte("now-3y");
		List<Object> products = ElasticUtils.constantScoreQuery("products")
				.queryBuilder(rangeQueryBuilder)
				.type(com.loserico.search.pojo.Product.class)
				.queryForList();
		
		products.forEach(p -> System.out.println(toJson(p)));
	}
	
	@Test
	public void testStructuredQuery2() {
		List<com.loserico.search.pojo.Product> products = ElasticUtils.constantScoreQuery("products")
				.queryBuilder(termQuery("avaliable", true))
				.type(com.loserico.search.pojo.Product.class)
				.queryForList();
		products.stream()
				.map(com.loserico.search.pojo.Product::isAvaliable)
				.forEach(System.out::println);
	}
	
	@Test
	public void testMatchQuery() {
		List<Movie> movies = ElasticUtils.query("movies")
				//查询结果纪要包含matrix, 又要包含reload
				.queryBuilder(matchQuery("title", "Matrix reloaded").operator(Operator.AND))
				.type(Movie.class)
				.queryForList();
		
		movies.forEach(movie -> System.out.println(toJson(movie)));
	}
	
	@Test
	public void testExistsField() {
		ElasticUtils.constantScoreQuery("products")
				.queryBuilder(existsQuery("date"))
				.type(com.loserico.search.pojo.Product.class)
				.queryForList()
				.forEach(System.out::println);
	}
	
	@Test
	public void testAgg() {
		SearchResponse searchResponse = ElasticUtils.client.prepareSearch("cars")
				.addAggregation(AggregationBuilders.count("popular_colors").field("color.keyword"))
				.get();
		Aggregations aggregations = searchResponse.getAggregations();
		System.out.println(toJson(aggregations));
	}
	
	/**
	 * apple pie
	 * apple Mac
	 * apple iPad
	 */
	@Test
	public void testSearchAppleCompany() {
		ElasticUtils.query("news")
				.queryBuilder(boolQuery()
						.must(matchQuery("content", "apple"))
						.mustNot(matchQuery("content", "pie")))
				.queryForList()
				.forEach(System.out::println);
	}
	
	/**
	 * 包含pie的文档贡献负分, 所以排到后面
	 * 演示了通过boosting控制排序
	 */
	@Test
	public void testBoostingquery() {
		MatchQueryBuilder positiveQuery = matchQuery("content", "apple");
		MatchQueryBuilder negaitiveQuery = matchQuery("content", "pie");
		BoostingQueryBuilder boostingQueryBuilder = boostingQuery(positiveQuery, negaitiveQuery);
		ElasticUtils.query("news")
				.queryBuilder(boostingQueryBuilder)
				.queryForList().forEach(System.out::println);
	}
	
	@Test
	public void testMultiMatch() {
		MultiMatchQueryBuilder multiMatchQueryBuilder = multiMatchQuery("Quick pets", "title", "body")
				.tieBreaker(0.2f)
				.minimumShouldMatch("20%");
		ElasticUtils.query("blogs")
				.queryBuilder(multiMatchQueryBuilder)
				.queryForList()
				.forEach(System.out::println);
	}
	
	@Test
	public void testMUltiMatchCrossField() {
		ElasticUtils.deleteIndex("address");
		ElasticUtils.index("address", "{\"street\": \"5 Poland Street\", \"city\": \"Lodon\", \"country\": \"United Kingdom\", \"postcode\": \"W1V 3DG\"}", "1");
		ElasticUtils.index("address", "{\"street\": \"5 Poland Street\", \"city\": \"Berminhan\", \"country\": \"United Kingdom\", \"postcode\": \"W2V 3DG\"}", "2");
		
		MultiMatchQueryBuilder queryBuilder = multiMatchQuery("Poland Street W1V", "street", "city", "country", "postcode")
				.type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
				.operator(Operator.AND);
		
		List<Object> address = ElasticUtils.query("address")
				.queryBuilder(queryBuilder)
				.queryForList();
		
		address.forEach(System.out::println);
	}
	
	@SneakyThrows
	@Test
	public void testHanLpAnalyzer() {
		AnalyzeResponse response =
				ElasticUtils.client.admin().indices().analyze(new AnalyzeRequest().text("美国会同意对台军售").analyzer(Analyzer.HANLP_NLP.toString())).get();
		response.getTokens().forEach((token) -> {
			System.out.println(token.getTerm());
		});
		System.out.println("------------------------");
		
		ElasticUtils.analyze(Analyzer.HANLP_STANDARD, "美国会同意对台军售").forEach(System.out::println);
		System.out.println("------------------------");
		ElasticUtils.analyze(Analyzer.HANLP, "美国会同意对台军售").forEach(System.out::println);
		System.out.println("------------------------");
		ElasticUtils.analyze(Analyzer.HANLP_N_SHORT, "美国会同意对台军售").forEach(System.out::println);
		System.out.println("------------------------");
	}
	
	@Test
	public void testFunctionScoreQuery() {
		ElasticUtils.deleteIndex("blogs");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 0 }", "1");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 100 }", "2");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 1000000 }", "3");
		
		List<Object> objects = ElasticUtils.functionScoreQuery(ScoreFunctionBuilders.fieldValueFactorFunction("votes"), "blogs")
				.boostMode(CombineFunction.SUM)
				.queryBuilder(multiMatchQuery("popularity", "title", "content"))
				.queryForList();
		
		objects.forEach(System.out::println);
		System.out.println("-----------------");
		ElasticUtils.query("blogs")
				.queryBuilder(multiMatchQuery("popularity", "title", "content"))
				.queryForList()
				.forEach(System.out::println);
	}
	
	@Test
	public void testRandomScoreQuery() {
		ElasticUtils.deleteIndex("blogs");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 0 }", "1");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 100 }", "2");
		ElasticUtils.index("blogs", "{\"title\": \"About popularity\", \"content\": \"In this post we will talk about...\", \"votes\": 1000000 }", "3");
		
		ElasticUtils.functionScoreQuery(ScoreFunctionBuilders.randomFunction().seed(666).setField("content"), "blogs")
				.queryBuilder(multiMatchQuery("popularity", "title", "content"))
				.queryForList()
				.forEach(System.out::println);
		
		ElasticUtils.functionScoreQuery(ScoreFunctionBuilders.randomFunction().seed(999).setField("content.keyword"), "blogs")
				.queryBuilder(multiMatchQuery("popularity", "title", "content"))
				.queryForList()
				.forEach(System.out::println);
	}
}
