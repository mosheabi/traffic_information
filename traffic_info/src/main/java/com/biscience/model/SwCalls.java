package com.biscience.model;

import com.biscience.TrafficInfoProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Anna Kuranda on 5/16/2017.
 */
public class SwCalls {
    public static final String SOURCE_DATA_CALL = "sourceTrafficData";
    private String request;
    private String response;
    private String url;
    public static final String TRAFFIC_CALL="traffic";
    private String callType;


    public SwCalls(String request, String response, String url,String callType) {
        this.request = request;
        this.response = response;
        this.url = url;
        this.callType = callType;
    }


    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static String getTrafficCall() {
        return TRAFFIC_CALL;
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();

        String separator = TrafficInfoProperties.CSV_SEPARATOR.getValue();
        if(StringUtils.isEmpty(separator))
        {
            separator = "\t";
        }

        sb.append(request).append(separator);
        sb.append(response).append(separator);
        sb.append(url).append(separator);
        sb.append(callType);
        return sb.toString();

    }
}
