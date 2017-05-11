package com.biscience.model;

import com.biscience.TrafficInfoProperties;
import com.google.common.collect.Maps;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Created by Anna Kuranda on 5/8/2017.
 */
public class PublisherTraffic {
    private int entityId;
    private String domain;
    private Boolean done;
    Map<Integer,Map<Integer,Boolean>> countryIdChanelStatusMap;


    public PublisherTraffic() {
        this.countryIdChanelStatusMap = Maps.newHashMap();
        done = false;

    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Map<Integer, Map<Integer, Boolean>> getCountryIdChanelStatusMap() {
        return countryIdChanelStatusMap;
    }

    public void setCountryIdChanelStatusMap(Map<Integer, Map<Integer, Boolean>> countryIdChanelStatusMap) {
        this.countryIdChanelStatusMap = countryIdChanelStatusMap;
    }

    public String toCsvLine(Integer countryId,Integer channelId , double estimatedPageViews,double monthlyVisitors,double bounceRate,double averagePageViews,double averageTimeViews){
        StringBuilder sb = new StringBuilder();
        sb.append(entityId).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(domain).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(countryId).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(channelId).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(estimatedPageViews).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(monthlyVisitors).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(bounceRate).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(averagePageViews).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(averageTimeViews).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(new Timestamp(new Date().getTime()));
        return  sb.toString();


    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
