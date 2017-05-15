package com.biscience.service;

import com.biscience.TrafficInfoProperties;
import com.biscience.model.PublisherTraffic;
import com.biscience.model.SwTrafficByCountry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import constants.TrafficSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import utils.HttpUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Anna Kuranda on 5/8/2017.
 */
public class PublisherTrafficInformationThread implements Callable {
    private PublisherTraffic publisherTraffic;
    private String infoDate;
    private String trafficUrlTemplate = "https://api.similarweb.com/v1/website/%p/Geo/traffic-by-country?api_key=%k&start_date=%d&end_date=%d&main_domain_only=false&format=json";
    private  final String SW_RECORDS = "records";
    // logger
    private static Logger logger = Logger.getLogger(PublisherTrafficInformationThread.class);
    private static Logger swRawDataLogger = Logger.getLogger("swDataCsv");
    private static Logger entityCountryLogger = Logger.getLogger("ecDataCsv");
    private String period;



    public PublisherTrafficInformationThread(PublisherTraffic publisherTraffic, String infoDate,String period) {
        logger.debug("Create thread for " + publisherTraffic.getDomain());
        this.publisherTraffic = publisherTraffic;
        this.infoDate = infoDate;
        this.period = period;
    }

    @Override
    public Boolean call() throws Exception {
        logger.debug("Start thread for " + publisherTraffic.getDomain());
        try {

            Map<Integer, SwTrafficByCountry> swTrafficByCountryMap = getSwTrafficData(publisherTraffic, infoDate);
            if (!swTrafficByCountryMap.isEmpty()) {
                for (Integer countryId : publisherTraffic.getCountryIdChanelStatusMap().keySet()) {
                    int isoCountryId  = TrafficInfoService.domainCountriesMap.get(countryId);
                    if(isoCountryId==0){
                        int parent = TrafficInfoService.domainParentMap.get(countryId);
                        logger.debug("Iso country code undefined. .... Take by parent id " +parent);
                        isoCountryId = TrafficInfoService.domainCountriesMap.get(parent);
                    }

                    logger.debug("Get sw data for iso country " + isoCountryId + " " +publisherTraffic.getDomain());
                    generateEcTrafficInfo(swTrafficByCountryMap.get(isoCountryId), publisherTraffic, countryId);

                }
            }
            else{
                logger.debug("Failed get sw traffic info for " + publisherTraffic.getDomain());
            }
            logger.debug("Finish thread with domain :" + publisherTraffic.getDomain());
        }catch(Exception e){
            logger.error("Failed run with domain "+  publisherTraffic.getDomain());
        }
        return true;
    }

    private void generateEcTrafficInfo(SwTrafficByCountry swTrafficByCountry, PublisherTraffic publisherTraffic, Integer countryId) {
        try {
            logger.info("Generate Entity country traffic info for publisher " +publisherTraffic.getDomain() + " in country " +countryId );
            if(swTrafficByCountry != null) {
                float estimatedPageView = Math.round(swTrafficByCountry.getVisits() * swTrafficByCountry.getPagesPerVisits());
                float monthlyVisitors = Math.round(swTrafficByCountry.getVisits());
                Map<Integer, Boolean> channelMap = publisherTraffic.getCountryIdChanelStatusMap().get(countryId);

                channelMap.keySet().forEach((Integer channel) -> {
                    String eclog = publisherTraffic.toCsvLine(countryId, channel, estimatedPageView, monthlyVisitors, swTrafficByCountry.getBounceRate(), swTrafficByCountry.getPagesPerVisits(), swTrafficByCountry.getAverageTime(),swTrafficByCountry.getShare(),period, TrafficSource.SW.name());
                    entityCountryLogger.info(eclog);
                    channelMap.put(channel, true);
                    publisherTraffic.setDone(true);

                });
            }else{
                logger.error("No SW information found for publisher "+publisherTraffic.getDomain() + " in country " + countryId);

            }
        }catch(Exception e){
            logger.error("Failed generate Entity country traffic info for publisher " +publisherTraffic.getDomain() + " in country " +countryId + e);
        }



    }


    private Map<Integer,SwTrafficByCountry> getSwTrafficData(PublisherTraffic publisherTraffic,String infoDate) {
        Map<Integer,SwTrafficByCountry> swData = Maps.newHashMap();
        try {

            trafficUrlTemplate = trafficUrlTemplate.replace("%d", infoDate).replace("%p", publisherTraffic.getDomain()).replace("%k", TrafficInfoProperties.SW_TOKEN.getValue().trim());
            logger.debug("Send request to "+trafficUrlTemplate);
            String swResponse = HttpUtil.getHttp(trafficUrlTemplate,TrafficInfoProperties.SOCKET_TIMEOUT.getIntValue());
            if(StringUtils.isEmpty(swResponse)){
                logger.error("Failed get response " + trafficUrlTemplate);
                logger.debug("Publisher should be not updated with SW info "+ publisherTraffic.getDomain());
            }
            else{
                swData = parseSwData(swResponse,publisherTraffic.getEntityId(),publisherTraffic.getDomain(),infoDate);
            }
        }catch(Exception e){
            logger.error("Failed get SW data " +e);
        }
        return swData;
    }

    private Map<Integer,SwTrafficByCountry> parseSwData( String swResponse, int entityId,String domain,String infoDate) {
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

    public PublisherTraffic getPublisherTraffic() {
        return publisherTraffic;
    }

    public void setPublisherTraffic(PublisherTraffic publisherTraffic) {
        this.publisherTraffic = publisherTraffic;
    }
}
