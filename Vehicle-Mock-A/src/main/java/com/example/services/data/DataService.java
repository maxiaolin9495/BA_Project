package com.example.services.data;

import com.example.data.DataRequest;
import com.example.data.DataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;



public class DataService {

    Logger log = LoggerFactory.getLogger(DataService.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${vehicle.data.endpoint}")
    private String vehicleDataEndpoint;


    public DataResponse sendData(String token){
        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.add(HttpHeaders.AUTHORIZATION, token);
        requestHeader.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<DataRequest> requestEntity = new HttpEntity<>(new DataRequest("This is a data message"), requestHeader);
        try {
            ResponseEntity<DataResponse> response = restTemplate.exchange(vehicleDataEndpoint, HttpMethod.POST, requestEntity, DataResponse.class);
            return response.getBody();
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            log.info("The data message is rejected. received following info: " + e.getMessage());
            throw new RuntimeException("Something unexpected happens");
        }
    }

    public DataResponse receiveData(DataRequest dataRequest){
        if(dataRequest.getMessage() != null){
            log.info(dataRequest.getMessage());
            return new DataResponse("This is a response message");
        }
        throw new RuntimeException("empty data message");
    }

}
