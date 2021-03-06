package com.loserico.search.builder;

import com.loserico.search.ElasticUtils;
import com.loserico.search.enums.Dynamic;
import com.loserico.search.enums.FieldType;
import com.loserico.search.support.FieldDef;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p>
 * Copyright: (C), 2020-12-28 8:54
 * <p>
 * <p>
 * Company: Information & Data Security Solutions Co., Ltd.
 *
 * @author Rico Yu ricoyu520@gmail.com
 * @version 1.0
 */
@Deprecated
@Slf4j
public final class PutMappingBuilder {
	
	private TransportClient client;
	
	/**
	 * 索引名
	 */
	private String index;
	
	/**
	 * 指定从这个索引复制Mapping设置
	 */
	private String copyIndex;
	
	/**
	 * 设置索引的dynamic属性: true false strict
	 */
	private Dynamic dynamic = null;
	
	/**
	 * 字段定义, 字段名-字段类型
	 */
	private Set<FieldDef> fields = new HashSet<>();
	
	/**
	 * 某些字段定义不需要的话恶意删除
	 */
	private Set<String> deleteFields = new HashSet<>();
	
	public PutMappingBuilder(TransportClient client, String index) {
		this.client = client;
		this.index = index;
	}
	
	/**
	 * 从一个索引中拷贝其mapping设置, 然后只需要显式设置某些字段的mapping, 减少coding量
	 *
	 * @param copyIndex
	 * @return PutMappingBuilder
	 */
	public PutMappingBuilder copy(String copyIndex) {
		this.copyIndex = copyIndex;
		return this;
	}
	
	/**
	 * 设置索引Mapping的dynamic属性: true false strict
	 *
	 * @param dynamic
	 * @return PutMappingBuilder
	 */
	public PutMappingBuilder dynamic(Dynamic dynamic) {
		this.dynamic = dynamic;
		return this;
	}
	
	/**
	 * 挨个设置字段类型
	 *
	 * @param fieldName
	 * @param fieldType
	 * @return PutMappingBuilder
	 */
	public PutMappingBuilder field(String fieldName, FieldType fieldType) {
		fields.add(new FieldDef(fieldName, fieldType));
		return this;
	}
	
	/**
	 * 挨个设置字段类型
	 *
	 * @param fieldName
	 * @param fieldType
	 * @param index     控制该字段是否被编入索引
	 * @return PutMappingBuilder
	 */
	public PutMappingBuilder field(String fieldName, FieldType fieldType, boolean index) {
		fields.add(new FieldDef(fieldName, fieldType, index));
		return this;
	}
	
	public PutMappingBuilder field(FieldDef fieldDef) {
		fields.add(fieldDef);
		return this;
	}
	
	public PutMappingBuilder delete(String... fields) {
		for (int i = 0; i < fields.length; i++) {
			deleteFields.add(fields[i]);
		}
		return this;
	}
	
	public boolean execute() {
		PutMappingRequestBuilder putMappingRequestBuilder = client.admin().indices().preparePutMapping(index);
		
		Map<String, Object> source = new HashMap<>();
		/*
		 * 从已有索引中拷贝mapping信息
		 */
		if (isNotBlank(copyIndex)) {
			source = ElasticUtils.getMapping(copyIndex);
		}
		
		/*
		 * 设置dynamic
		 */
		if (dynamic != null) {
			source.put("dynamic", dynamic);
		}
		
		//先取出properties, 如果没有, 那么创建一个
		Map<String, Object> properties = (Map<String, Object>) source.get("properties");
		if (properties == null) {
			properties = new HashMap<>();
			source.put("properties", properties);
		}
		
		//去掉要删除的字段定义
		if (!deleteFields.isEmpty() && !properties.isEmpty()) {
			for (String fieldName : deleteFields) {
				properties.remove(fieldName);
			}
		}
		
		/*
		 * 挨个设置字段定义
		 */
		if (!fields.isEmpty()) {
			for (FieldDef fieldDef : fields) {
				properties.put(fieldDef.getFieldName(), fieldDef.toDefMap());
			}
		}
		
		AcknowledgedResponse acknowledgedResponse = putMappingRequestBuilder.setType(ElasticUtils.ONLY_TYPE)
				.setSource(source)
				.get();
		return acknowledgedResponse.isAcknowledged();
	}
	
}
