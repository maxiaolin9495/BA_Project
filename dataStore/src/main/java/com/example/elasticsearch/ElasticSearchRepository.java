package com.example.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.example.data.CA;
import com.example.data.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class ElasticSearchRepository {

    Logger logger = LoggerFactory.getLogger(ElasticSearchRepository.class);

    @Autowired
    private RestHighLevelClient highLevelClient;

    ObjectMapper objectMapper = new ObjectMapper();

    public Vehicle findVehicle(String index, String vin) {


        GetRequest getRequest = new GetRequest(index, vin);
        try {
            GetResponse response = highLevelClient.get(getRequest, RequestOptions.DEFAULT);
            if(response.isExists()){
                return objectMapper.readValue(response.getSourceAsBytes(), Vehicle.class);
            }
            return null;
        }catch (IOException e){
            logger.error("error happens, when trying to find the event in elasticsearch");
            return null;
        }catch(ElasticsearchStatusException e){
            logger.info("No such index");
            return null;
        }
    }


    public IndexResponse addVehicle(String index, Vehicle vehicle) throws IOException {
        IndexRequest request = new IndexRequest(index).id(vehicle.getVin() + "").source(JSON.toJSONString(vehicle), XContentType.JSON);

        request.timeout(TimeValue.timeValueSeconds(10));
        IndexResponse indexResponse = highLevelClient.index(request, RequestOptions.DEFAULT);
        DocWriteResponse.Result indexResponseResult = indexResponse.getResult();
        //if (indexResponseResult == DocWriteResponse.Result.UPDATED) {
        //    logger.warn("Overwritten in event " + event.getId() + " reasons" + indexResponse.getShardInfo());
        //}
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
                logger.warn("failure reasons", reason);
            }
        }
        return indexResponse;

    }

    public CA findCA(String index, String caId) {


        GetRequest getRequest = new GetRequest(index, caId);
        try {
            GetResponse response = highLevelClient.get(getRequest, RequestOptions.DEFAULT);
            if(response.isExists()){
                return objectMapper.readValue(response.getSourceAsBytes(), CA.class);
            }
            return null;
        }catch (IOException e){
            logger.error("error happens, when trying to find the event in elasticsearch");
            return null;
        }catch(ElasticsearchStatusException e){
            logger.info("No such index");
            return null;
        }
    }

    public IndexResponse addCA(String index, CA ca) throws IOException {
        IndexRequest request = new IndexRequest(index).id(ca.getCaId() + "").source(JSON.toJSONString(ca), XContentType.JSON);

        request.timeout(TimeValue.timeValueSeconds(10));
        IndexResponse indexResponse = highLevelClient.index(request, RequestOptions.DEFAULT);
        DocWriteResponse.Result indexResponseResult = indexResponse.getResult();
        //if (indexResponseResult == DocWriteResponse.Result.UPDATED) {
        //    logger.warn("Overwritten in event " + event.getId() + " reasons" + indexResponse.getShardInfo());
        //}
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
                logger.warn("failure reasons", reason);
            }
        }
        return indexResponse;

    }

}
