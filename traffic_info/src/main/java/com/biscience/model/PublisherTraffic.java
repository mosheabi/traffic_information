package com.biscience.model;

import com.biscience.TrafficInfoProperties;
import com.google.common.collect.Maps;
import constants.TrafficInfoCmdParams;
import org.apache.commons.lang3.StringUtils;

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
/*
entity_id DECIMAL(18,0),
PUB_NAME VARCHAR(1024),
COUNTRY_ID DECIMAL(18,0),
CHANNEL DECIMAL(2,0),
estimated_page_views  DECIMAL(18,5),
monthly_visitors  DECIMAL(18,5),
Bounce_rate  DECIMAL(18,5),
average_page_views DECIMAL(18,5),
average_time_spent DECIMAL(18,5),
Info_date TIMESTAMP,
Month_id DECIMAL(6,0),
estimated_Visitors DECIMAL(18,5)
 */
    public String toCsvLine(Integer countryId,Integer channelId , double estimatedPageViews,double monthlyVisitors,double bounceRate,double averagePageViews,double averageTimeViews,String monthId,Double estimatedVisitors){
        StringBuilder sb = new StringBuilder();
        String separator = StringUtils.isEmpty(TrafficInfoProperties.CSV_SEPARATOR.getValue())? TrafficInfoCmdParams.CSV_DEFAULT_DELIMITER : TrafficInfoProperties.CSV_SEPARATOR.getValue();
        sb.append(entityId).append(separator);
        sb.append(domain).append(separator);
        sb.append(countryId).append(separator);
        sb.append(channelId).append(separator);
        sb.append(estimatedPageViews).append(separator);
        sb.append(monthlyVisitors).append(separator);
        sb.append(bounceRate).append(separator);
        sb.append(averagePageViews).append(separator);
        sb.append(averageTimeViews).append(separator);
        sb.append(new Timestamp(new Date().getTime())).append(separator);
        sb.append(monthId).append(separator);
        sb.append(estimatedVisitors);

        return  sb.toString();


    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
