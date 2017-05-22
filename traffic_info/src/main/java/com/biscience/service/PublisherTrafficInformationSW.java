package com.biscience.service;

import com.biscience.SwApiParser;
import com.biscience.TrafficInfoProperties;
import com.biscience.model.*;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import utils.HttpUtil;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Anna Kuranda on 5/8/2017.
 */
public class PublisherTrafficInformationSW implements Callable {
    private PublisherTraffic publisherTraffic;
    private String infoDate;
    private String trafficUrlTemplate = "https://api.similarweb.com/v1/website/%p/Geo/traffic-by-country?api_key=%k&start_date=%d&end_date=%d&main_domain_only=false&format=json";


    private String sourceDataUrlTemplate = "https://api.similarweb.com/v1/website/%p/traffic-sources/overview-share?api_key=%k&start_date=%d&end_date=%d&main_domain_only=false";
    // logger
    private static Logger logger = Logger.getLogger(PublisherTrafficInformationSW.class);
    private static Logger entityCountryLogger = Logger.getLogger("ecDataCsv");
    private static Logger swCallsLogger= Logger.getLogger("swCallsCsv");
    private static Logger publisherTrafficSourceLogger= Logger.getLogger("SwSourceData");

    private String monthId;
    private HttpUtil httpUtil;
    private SwApiParser swApiParser;



    public PublisherTrafficInformationSW(PublisherTraffic publisherTraffic, String infoDate, String monthId) {
        logger.debug("Create thread for " + publisherTraffic.getDomain());
        this.publisherTraffic = publisherTraffic;
        this.infoDate = infoDate;
        this.monthId = monthId;
        httpUtil = new HttpUtil();
        swApiParser = new SwApiParser();
    }

    @Override
    public Boolean call() throws Exception {
        logger.debug("Start thread for " + publisherTraffic.getDomain());
        try {

            generateDataWithSWTrafficApi();
            generateDataWithSWTrafficSourceApi();
            logger.debug("Finish thread with domain :" + publisherTraffic.getDomain());
        }catch(Exception e){
            logger.error("Failed run with domain "+  publisherTraffic.getDomain());
        }
        return true;
    }

    private void generateDataWithSWTrafficSourceApi() {
        Map<String,Object> swTrafficSourceMap = getSwTrafficSourceData(publisherTraffic, infoDate);
        if (!swTrafficSourceMap.isEmpty()) {
            Double sum = (Double) swTrafficSourceMap.get(SwApiParser.SUMMERY_KEY);
            logger.debug("Sum of all paid and organic from all type "+sum);
            swTrafficSourceMap.remove(SwApiParser.SUMMERY_KEY);
            swTrafficSourceMap.keySet().forEach(swTrafficSourceType->{
                SwTrafficSource swTrafficSourceValue = (SwTrafficSource) swTrafficSourceMap.get(swTrafficSourceType);
                Double srcShare = swTrafficSourceValue.getOrganicWithPaid() / sum;
                logger.debug("Src share for domain "+publisherTraffic.getDomain()+" is "+srcShare );
                PublisherTrafficSource publisherTrafficSource = new PublisherTrafficSource(publisherTraffic.getEntityId(),swTrafficSourceType,srcShare,publisherTraffic.getDomain());
                publisherTrafficSourceLogger.info(publisherTrafficSource.toCsv());
            });
        }

        else{
            logger.debug("Failed get sw source traffic info for " + publisherTraffic.getDomain());
        }
    }

    private void generateDataWithSWTrafficApi(){

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


    }

    private void generateEcTrafficInfo(SwTrafficByCountry swTrafficByCountry, PublisherTraffic publisherTraffic, Integer countryId) {
        try {
            logger.info("Generate Entity country traffic info for publisher " +publisherTraffic.getDomain() + " in country " +countryId );
            if(swTrafficByCountry != null) {
                float estimatedPageView = Math.round(swTrafficByCountry.getVisits() * swTrafficByCountry.getPagesPerVisits());
                float monthlyVisitors = Math.round(swTrafficByCountry.getVisits());
                Map<Integer, Boolean> channelMap = publisherTraffic.getCountryIdChanelStatusMap().get(countryId);
                double estimatedVisitors = swTrafficByCountry.getVisits()/swTrafficByCountry.getShare();
                channelMap.keySet().forEach((Integer channel) -> {
                    String eclog = publisherTraffic.toCsvLine(countryId, channel, estimatedPageView, monthlyVisitors, swTrafficByCountry.getBounceRate(), swTrafficByCountry.getPagesPerVisits(), swTrafficByCountry.getAverageTime(), monthId, estimatedVisitors);
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



    private Map<String,Object> getSwTrafficSourceData(PublisherTraffic publisherTraffic, String infoDate) {
        Map<String,Object> swDataTrafficSource = Maps.newHashMap();
        try {

            sourceDataUrlTemplate = sourceDataUrlTemplate.replace("%d", infoDate).replace("%p", httpUtil.getUrlWithProtocol(publisherTraffic.getDomain())).replace("%k", TrafficInfoProperties.SW_TOKEN.getValue().trim());
            logger.debug("Send request to "+trafficUrlTemplate);
            Pair<Integer, String> swResponse = httpUtil.getHttp(sourceDataUrlTemplate,TrafficInfoProperties.SOCKET_TIMEOUT.getIntValue());
            SwCalls swCalls = new SwCalls(sourceDataUrlTemplate,swResponse.getRight(),publisherTraffic.getDomain(),SwCalls.SOURCE_DATA_CALL );
            swCallsLogger.info(swCalls.toCsv());
            if(swResponse.getLeft()!= HttpStatus.SC_OK || StringUtils.isEmpty(swResponse.getRight())){
                logger.error("Failed get response " + sourceDataUrlTemplate);
                logger.debug("Publisher should be not updated with SW source info "+ publisherTraffic.getDomain());
            }
            else{
                swDataTrafficSource = swApiParser.parseSwTrafficSourceData(swResponse.getRight(),httpUtil.getUrlWithProtocol(publisherTraffic.getDomain()),infoDate);
            }
        }catch(Exception e){
            logger.error("Failed get SW data " +e);
        }
        return swDataTrafficSource;
    }



    private Map<Integer,SwTrafficByCountry> getSwTrafficData(PublisherTraffic publisherTraffic,String infoDate) {
        Map<Integer,SwTrafficByCountry> swData = Maps.newHashMap();
        try {

            trafficUrlTemplate = trafficUrlTemplate.replace("%d", infoDate).replace("%p", httpUtil.getUrlWithProtocol(publisherTraffic.getDomain())).replace("%k", TrafficInfoProperties.SW_TOKEN.getValue().trim());
            logger.debug("Send request to "+trafficUrlTemplate);
            Pair<Integer, String> swResponse = httpUtil.getHttp(trafficUrlTemplate,TrafficInfoProperties.SOCKET_TIMEOUT.getIntValue());
            SwCalls swCalls = new SwCalls(trafficUrlTemplate,swResponse.getRight(),publisherTraffic.getDomain(),SwCalls.TRAFFIC_CALL );
            swCallsLogger.info(swCalls.toCsv());
            if(swResponse.getLeft()!= HttpStatus.SC_OK || StringUtils.isEmpty(swResponse.getRight())){
                logger.error("Failed get response " + trafficUrlTemplate);
                logger.debug("Publisher should be not updated with SW info "+ publisherTraffic.getDomain());
            }
            else{
                swData = swApiParser.parseSwData(swResponse.getRight(),publisherTraffic.getEntityId(),httpUtil.getUrlWithProtocol(publisherTraffic.getDomain()),infoDate);
            }
        }catch(Exception e){
            logger.error("Failed get SW data " +e);
        }
        return swData;
    }



    public PublisherTraffic getPublisherTraffic() {
        return publisherTraffic;
    }

    public void setPublisherTraffic(PublisherTraffic publisherTraffic) {
        this.publisherTraffic = publisherTraffic;
    }


}
