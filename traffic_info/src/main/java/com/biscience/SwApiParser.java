package com.biscience;

import com.biscience.model.SwTrafficByCountry;
import com.biscience.model.SwTrafficSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by Anna Kuranda on 5/21/2017.
 */
public class SwApiParser {
    private  final String SW_RECORDS = "records";
    private final String SW_VISITS = "visits";
    private final String SW_ORGANIC = "organic";
    private final String SW_PAID = "paid";
    private final String SW_SOURCE_TYPE = "source_type";
    public final static String SUMMERY_KEY="sum";

    private static Logger swRawDataLogger = Logger.getLogger("swDataCsv");
    private static Logger swSourceRawDataLogger = Logger.getLogger("swSourceDataCsv");
    // logger
    private static Logger logger = Logger.getLogger(SwApiParser.class);

    public Map<Integer,SwTrafficByCountry> parseSwData(String swResponse, int entityId, String domain, String infoDate) {
        Map<Integer,SwTrafficByCountry> swTrafficInfoMap = Maps.newHashMap();
        try {
            if (!StringUtils.isEmpty(swResponse)){
                Map mapResponse  = new ObjectMapper().readValue(swResponse,Map.class);
                List<Map<String,Object>> swCountriesData = (List<Map<String, Object>>) mapResponse.get(SW_RECORDS);
                swCountriesData.forEach(swCountryData->{
                    SwTrafficByCountry swTrafficByCountry = new SwTrafficByCountry(swCountryData,infoDate,domain,entityId);
                    swRawDataLogger.info(swTrafficByCountry.toCsvLine());
                    swTrafficInfoMap.put(swTrafficByCountry.getCountry(),swTrafficByCountry);





                });
            }
        }catch(Exception e){
            logger.error("Failed parse data from SW "+e);
        }
        return  swTrafficInfoMap;
    }
    public  Map<String,Object> parseSwTrafficSourceData(String swResponse, String domain, String infoDate) {
        Map<String,Object> swTrafficSourceMap = Maps.newHashMap();
        swTrafficSourceMap.put(SUMMERY_KEY,0.0);

        try {
            if (!StringUtils.isEmpty(swResponse)){
                Map mapResponse  = new ObjectMapper().readValue(swResponse,Map.class);

                List<Map> sourceTypeMapList = (List<Map>) ((Map) mapResponse.get(SW_VISITS)).get(domain);
                if(sourceTypeMapList!=null &&!sourceTypeMapList.isEmpty()) {

                    sourceTypeMapList.forEach(sourceTypeMap -> {
                        String sourceType = "";
                        Double sourcePaid = null;
                        Double sourceOrganic = null;
                        if (sourceTypeMap.containsKey(SW_SOURCE_TYPE)) {
                            sourceType = (String) sourceTypeMap.get(SW_SOURCE_TYPE);
                        }
                        if (sourceTypeMap.containsKey(SW_VISITS)) {
                            Map<String, Double> sourceValuesMap = (Map<String, Double>) ((List) sourceTypeMap.get(SW_VISITS)).get(0);
                            sourcePaid = sourceValuesMap.get(SW_PAID);
                            sourceOrganic = sourceValuesMap.get(SW_ORGANIC);


                        }
                        if (!StringUtils.isEmpty(sourceType) && sourceOrganic != null && sourcePaid != null) {
                            SwTrafficSource swTrafficSource = new SwTrafficSource(sourceType, sourceOrganic, sourcePaid, domain, infoDate);
                            swSourceRawDataLogger.info(swTrafficSource.toCsv());
                            swTrafficSourceMap.put(sourceType, swTrafficSource);
                            swTrafficSourceMap.put(SUMMERY_KEY,(Double)swTrafficSourceMap.get(SUMMERY_KEY)+sourcePaid+sourceOrganic);

                        } else {
                            logger.error("Failed get source data for " + domain );
                        }
                    });

                }
                else{
                    logger.error("Failed parse response from get source data api " + domain );
                }



            }
        }catch(Exception e){
            logger.error("Failed parse data from SW "+e);
        }
        return  swTrafficSourceMap;
    }

}
