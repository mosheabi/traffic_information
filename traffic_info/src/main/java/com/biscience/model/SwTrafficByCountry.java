package com.biscience.model;


import com.biscience.TrafficInfoProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Anna Kuranda on 5/8/2017.
 */
//"records":[{"country":840,"share":0.17879871824385313,"visits":2375278932.5413823,"pages_per_visit":11.492052113430203,"average_time":898.26596133680846,"bounce_rate":0.21777055100364145,"rank":2}
public class SwTrafficByCountry {
    private String startDate;
    private String endDate;
    private int country;
    private double share;
    private double visits;
    private double pagesPerVisits;
    private double averageTime;
    private double bounceRate;
    private int rank;
    private String domain;
    private int entityId;

    public SwTrafficByCountry(Map<String,Object> swInfo,String infoDate,String domain,int entityId) {
        startDate = infoDate;
        endDate = infoDate;
        country = Integer.parseInt(swInfo.get("country").toString());
        share = Double.parseDouble(swInfo.get("share").toString());
        visits = Double.parseDouble(swInfo.get("visits").toString());
        pagesPerVisits = Double.parseDouble(swInfo.get("pages_per_visit").toString());
        averageTime = Double.parseDouble(swInfo.get("average_time").toString());
        bounceRate = Double.parseDouble(swInfo.get("bounce_rate").toString());
        rank = Integer.parseInt(swInfo.get("rank").toString());
        this.entityId = entityId;
        this.domain = domain;


    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }

    public double getVisits() {
        return visits;
    }

    public void setVisits(double visits) {
        this.visits = visits;
    }

    public double getPagesPerVisits() {
        return pagesPerVisits;
    }

    public void setPagesPerVisits(double pagesPerVisits) {
        this.pagesPerVisits = pagesPerVisits;
    }

    public double getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(double averageTime) {
        this.averageTime = averageTime;
    }

    public double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public static void main(String[] args) throws IOException {
        String response  = "{\"meta\":{\"request\":{\"main_domain_only\":false,\"format\":\"json\",\"domain\":\"facebook.com\",\"start_date\":\"2017-01-01\",\"end_date\":\"2017-01-31\",\"limit\":null,\"country\":\"world\"},\"status\":\"Success\",\"last_updated\":\"2017-03-31\"},\"records\":[{\"country\":840,\"share\":0.17879871824385313,\"visits\":2375278932.5413823,\"pages_per_visit\":11.492052113430203,\"average_time\":898.26596133680846,\"bounce_rate\":0.21777055100364145,\"rank\":2}]}";
        Map mapResponse  = new ObjectMapper().readValue(response,Map.class);

        List<Map<String,Integer>> swCountriesData = (List<Map<String, Integer>>) mapResponse.get("records");

        System.out.println("done");
    }
    ////"records":[{"country":840,"share":0.17879871824385313,"visits":2375278932.5413823,"pages_per_visit":11.492052113430203,"average_time":898.26596133680846,"bounce_rate":0.21777055100364145,"rank":2}

    public String toCsvLine(){
        StringBuilder sb = new StringBuilder();
        sb.append(entityId).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(domain).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(country).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(share).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(visits).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(pagesPerVisits).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(averageTime).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(bounceRate).append(TrafficInfoProperties.CSV_SEPARATOR.getValue());
        sb.append(rank);


        return sb.toString();

    }

}
