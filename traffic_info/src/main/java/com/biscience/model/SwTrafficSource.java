package com.biscience.model;

import com.biscience.TrafficInfoProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Anna Kuranda on 5/21/2017.
 */
public class SwTrafficSource {
    private String sourceType;
    private Double organic;
    private Double paid;
    private String domain;
    private String infoDate;
    private Double organicWithPaid;

    public SwTrafficSource(String sourceType, Double organic, Double paid, String domain, String infoDate) {
        this.sourceType = sourceType;
        this.organic = organic;
        this.paid = paid;
        this.domain = domain;
        this.infoDate = infoDate;
        this.organicWithPaid = organic+paid;
    }

    public Double getOrganicWithPaid() {
        return organicWithPaid;
    }

    public void setOrganicWithPaid(Double organicWithPaid) {
        this.organicWithPaid = organicWithPaid;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getInfoDate() {
        return infoDate;
    }

    public void setInfoDate(String infoDate) {
        this.infoDate = infoDate;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Double getOrganic() {
        return organic;
    }

    public void setOrganic(Double organic) {
        this.organic = organic;
    }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }


    public String toCsv(){
        StringBuilder sb = new StringBuilder();

        String separator = TrafficInfoProperties.CSV_SEPARATOR.getValue();
        if(StringUtils.isEmpty(separator))
        {
            separator = "\t";
        }

        sb.append(infoDate).append(separator);
        sb.append(domain).append(separator);
        sb.append(sourceType).append(separator);
        sb.append(organic).append(separator);
        sb.append(paid);

        return sb.toString();

    }


    public static void main (String[] args) throws IOException {
        String response = "{\"meta\":{\"request\":{\"main_domain_only\":false,\"format\":null,\"domain\":\"cnn.com\",\"start_date\":\"2016-01-01\",\"end_date\":\"2016-01-31\",\"limit\":null,\"country\":\"world\"},\"status\":\"Success\",\"last_updated\":\"2017-04-30\"},\"visits\":{\"cnn.com\":[{\"source_type\":\"Search\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":51458160.205486923,\"paid\":937.97671990549918}]},{\"source_type\":\"Social\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":22479942.231019154,\"paid\":0.0}]},{\"source_type\":\"Mail\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":1856108.1458228377,\"paid\":0.0}]},{\"source_type\":\"Display Ads\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":0.0,\"paid\":433324.13725889189}]},{\"source_type\":\"Direct\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":119538234.71487789,\"paid\":0.0}]},{\"source_type\":\"Referrals\",\"visits\":[{\"date\":\"2016-01-01\",\"organic\":23387727.446267541,\"paid\":0.0}]}]}}";
        Map mapResponse  = new ObjectMapper().readValue(response,Map.class);
        String domain = "cnn.com";
        List<Map> sourceTypeMapList = (List<Map>) ((Map) mapResponse.get("visits")).get(domain);
        sourceTypeMapList.forEach(sourceTypeMap->{
            if(sourceTypeMap.containsKey("source_type")){
                System.out.println(sourceTypeMap.get("source_type"));
            }
            if(sourceTypeMap.containsKey("visits")){
                Map<String,Double> sourceValuesMap = (Map<String, Double>) ((List)sourceTypeMap.get("visits")).get(0);
                System.out.println(sourceValuesMap.get("organic"));
                System.out.println(sourceValuesMap.get("paid"));
            }

        });


        //List<Map<String,Integer>> swCountriesData = (List<Map<String, Integer>>) mapResponse.get("visits").get(domain);
        System.out.println("Done");
    }
}
