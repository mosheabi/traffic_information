package com.biscience.service;

import com.biscience.EntitiesDao;
import com.biscience.TrafficInfoProperties;
import com.biscience.model.PublisherTraffic;
import com.google.common.collect.Maps;
import constants.TrafficInfoCmdParams;
import monitoring.counters.dynamic.CounterManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */

@Service
public class TrafficInfoService {


	private static Logger logger = Logger.getLogger(TrafficInfoService.class);
	// counters
	public static CounterManager counterManager;



	//country ->iso country
	public static Map<Integer,Integer> domainCountriesMap;
	public static Map<Integer,Integer> domainParentMap;
	public static Map<String,PublisherTraffic>  domainTrafficMap;
	//public static StringBuffer msgText;
	public static Boolean upgradeSwAccount ;





	public void init(){

		domainCountriesMap = Maps.newHashMap();
		domainTrafficMap = Maps.newConcurrentMap();
		domainParentMap = Maps.newHashMap();
		EntitiesDao.getRelevantPublishersForSW();
		EntitiesDao.getCountries();
		counterManager = new CounterManager();
		//msgText = new StringBuffer();
		upgradeSwAccount = new Boolean(false);


	}

	public void execute() {
		String infoDate = getInfoDate();
		System.out.println("Start app");
		if( domainTrafficMap.isEmpty() || domainCountriesMap.isEmpty()){
			logger.error("Failed get data from DB .Can't continue traffic information flow");
			return;
		}
		if(StringUtils.isEmpty(infoDate)){
			logger.error("Failed generate info date for SW api request");
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(TrafficInfoProperties.NUM_PROCESSES.getIntValue());
		Set<Future<Boolean> >futureSet = Collections.synchronizedSet(new HashSet<Future<Boolean>>());
		int publishersNumberNotRunned = domainTrafficMap.keySet().size();

		try {
			for (String domain : domainTrafficMap.keySet()){

				logger.info("RUN :"+domain);
				String period = infoDate.replace(TrafficInfoCmdParams.SW_DATE_DELIMITER,"");
				PublisherTrafficInformationSW publisherTrafficInformationSW = new PublisherTrafficInformationSW(domainTrafficMap.get(domain), infoDate,period);

				Future<Boolean> future = executor.submit(publisherTrafficInformationSW);
				futureSet.add(future);
				publishersNumberNotRunned--;
			}

			futureSet.forEach(f -> {
				try {
					//get analised urls(after check domain)
					Boolean res = f.get();
				}
				catch (Exception e1) {
					logger.error("Interruption issue " + e1);
				}
			});

		} catch (Exception e) {
			logger.error("Failed in updates domains ");
		}
		finally {
			executor.shutdownNow();

		}




		logger.info("----- Run Summery ----");
		logger.info("Time : " +new Date());
		//in first index should sum all records to update
		//in second all updated records
		//in third all not updated records
		final int[] doneCounts = new int[3];

		domainTrafficMap.keySet().forEach((String domain) ->{
			Map<Integer, Map<Integer, Boolean>> countryIdChanelStatusMap = domainTrafficMap.get(domain).getCountryIdChanelStatusMap();

			countryIdChanelStatusMap.keySet().forEach(countryIdChanelStatus->{
				Map<Integer,Boolean> countriesChannel = countryIdChanelStatusMap.get(countryIdChanelStatus);
				countriesChannel.keySet().forEach(chanelStatus->{
					doneCounts[0]++;
					counterManager.inc(CounterManager.Types.PUBL_RECEIVED);
					if(countriesChannel.get(chanelStatus)) {
						doneCounts[1]++;
						counterManager.inc(CounterManager.Types.PUBL_COMPLETED);

					}

					else{
						doneCounts[2]++;
						counterManager.inc(CounterManager.Types.PUBL_FAILED);
					}

				});
			});

		});
		//for test msgText

		if(upgradeSwAccount.booleanValue())
		{
			sendMail();

		}
		logger.info("Found for updates "+ doneCounts[0]);
		logger.info("Succeded updates "+ doneCounts[1]);
		logger.info("Failed updates "+ doneCounts[2]);







	}

	private void sendMail() {
		SendMail sendMail = new SendMail();
		String subject = TrafficInfoProperties.MAIL_SUBJECT.getValue();
		sendMail.send(subject,TrafficInfoProperties.MAIL_MSG.getValue());
	}


	private String getInfoDate() {

		String infoDate = null;
		try {
			Calendar calendar = Calendar.getInstance();
			//month values from 0-11
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			//Calendar.JANUARY = 0
			if (month == Calendar.JANUARY ) {
				year--;
				month = Calendar.DECEMBER + 1;
			}
			String monthStr = String.valueOf(month);
			monthStr = (month<10 && monthStr.length() == 1 ) ? "0"+monthStr : monthStr;
			infoDate = year + TrafficInfoCmdParams.SW_DATE_DELIMITER + monthStr;
		}catch (Exception e)
		{
			logger.error("Failed compute info date "+e);
		}
		return infoDate;
	}






}
