package com.qbian.common.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Qbian on 2017/5/11.
 */
@Component
public class ElasticSearchClient {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchClient.class);

    /**
     * 文档类型名称 =》 数据库名
     */
    private static final String INDEX_TYPE = "test";

    /**
     * client
     */
    private static TransportClient client;

    /**
     * 集群节点 ip 数组
     */
    @Value("${es.ips}")
    private String ipStr;

    /**
     * 集群内节点通讯端口
     */
    @Value("${es.port}")
    private Integer port;

    /**
     * 集群名称
     */
    @Value("${es.cluster}")
    private String clusterName;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        try {
            // 设置集群名称
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();

            client = new PreBuiltTransportClient(settings);
            if(!StringUtils.isEmpty(ipStr)) {
                String[] ips = ipStr.split(",");
                for(String ip : ips) {
                    if(!StringUtils.isEmpty(ip)) {
                        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("ElasticSearchClient init error .", e);
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void close() {
        try {
            if(client != null) {
                client.close();
            }
        } catch (Exception e) {
            LOG.error("ElasticSearchClient close error .", e);
        }
    }

    /**
     * 创建索引，保存数据
     * @param type 文档类型
     * @param jsonData json 格式的数据
     */
    public void  createIndex(String type, JSONObject jsonData) {
        IndexRequestBuilder requestBuilder = client.prepareIndex(INDEX_TYPE, type);
        requestBuilder.setSource(jsonData.toJSONString()).execute().actionGet();
    }

    /**
     * 检索集群
     * @param queryBuilder 查询引擎
     * @param type 查询文档类型
     * @param pageNo 起始页
     * @param pageSize 当页数量
     * @return
     */
    public JSONArray search(QueryBuilder queryBuilder, String type, int pageNo, int pageSize) {
        // 检索集群数据
        SearchResponse searchResponse = client.prepareSearch(INDEX_TYPE).setTypes(type)
                .setQuery(queryBuilder).addSort("date", SortOrder.DESC)
                .setFrom(pageNo * pageSize).setSize(pageSize)
                .execute().actionGet();
        SearchHit[] hits = searchResponse.getHits().getHits();

        // 封装检索结果
        JSONArray result = new JSONArray();
        if(hits != null && hits.length > 0) {
            for(SearchHit hit : hits) {
                JSONObject data = new JSONObject();
                for(Entry<String, Object> entry : hit.getSource().entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
                result.add(data);
            }
        }

        return result;
    }

    /**
     * 检索匹配关键字高亮
     * @param queryBuilder 查询引擎
     * @param type 文档类型
     * @param highlightFieldList 高亮清单
     * @param pageNo 页码
     * @param pageSize 当页显示数据量
     * @return 查询结果
     */
    public JSONArray searchHighlight(QueryBuilder queryBuilder, String type, List<String> highlightFieldList
            , int pageNo, int pageSize) {
        StopWatch clock = new StopWatch();
        clock.start();

        // 设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().requireFieldMatch(true);
        if(highlightFieldList != null) {
            for(String field : highlightFieldList) {
                highlightBuilder.field(field, 10240);
            }
        }
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");

        SearchResponse searchResponse = client.prepareSearch(INDEX_TYPE).setTypes(type)
                .setQuery(queryBuilder).highlighter(highlightBuilder)
                .addSort("date", SortOrder.DESC)
                .setFrom(pageNo * pageSize).setSize(pageSize)
                .setExplain(true).execute().actionGet();
        SearchHit[] hits = searchResponse.getHits().getHits();

        clock.stop();
        LOG.info("searchHighlight: {} ms", clock.getTotalTimeMillis());

        // 封装查询结果
        JSONArray result = new JSONArray();
        if(hits != null && hits.length > 0) {
            for(SearchHit hit : hits) {
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                JSONObject data = new JSONObject();
                for(Entry<String, Object> entry : hit.getSource().entrySet()) {
                    // 保存高亮字段
                    if(highlightFields != null && highlightFields.containsKey(entry.getKey())) {
                        HighlightField titleField = highlightFields.get(entry.getKey());
                        Text[] fragments = titleField.fragments();
                        StringBuilder sb = new StringBuilder();
                        for(Text text : fragments) {
                            sb.append(text);
                        }
                        data.put(entry.getKey(), sb.toString());
                    } else {
                        data.put(entry.getKey(), entry.getValue());
                    }

                }
                result.add(data);
            }
        }

        return result;
    }

}
