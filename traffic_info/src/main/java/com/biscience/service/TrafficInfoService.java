package com.biscience.service;

import com.biscience.EntitiesDao;
import com.biscience.TrafficInfoProperties;
import com.biscience.model.PublisherTraffic;
import com.google.common.collect.Maps;
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



	//country ->iso country
	public static Map<Integer,Integer> domainCountriesMap;
	public static Map<String,PublisherTraffic>  domainTrafficMap;





	public void init(){

		domainCountriesMap = Maps.newHashMap();
		domainTrafficMap = Maps.newConcurrentMap();
		EntitiesDao.getRelevantPublishersForSW();
		EntitiesDao.getCountries();



	}
	//@Scheduled  // every 30 seconds
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
				PublisherTrafficInformationThread publisherTrafficInformationThread = new PublisherTrafficInformationThread(domainTrafficMap.get(domain), infoDate);

				Future<Boolean> future = executor.submit(publisherTrafficInformationThread);
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

//		while( publishersNumberNotRunned > 0 || !isComplete(futureSet)){
//
//			///wait
//			try {
//				logger.debug( "Still running with publishers... ");
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				logger.debug("was some interrupt "+e);
//			}
//
//		}


		logger.info("----- Run Summery ----");
		logger.info("Time : " +new Date());
		final int[] doneCounts =new int[2];

		domainTrafficMap.keySet().forEach((String domain) ->{
			if(domainTrafficMap.get(domain).getDone()){
				doneCounts[0]++;
			}
			else{
				doneCounts[1]++;
			}
		});
		logger.info("Received publishers " +domainTrafficMap.keySet().size());
		logger.info("Succeded updates "+ doneCounts[0]);
		logger.info("Failed updates "+ doneCounts[1]);

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
			infoDate = year + "-" + month;
		}catch (Exception e)
		{
			logger.error("Failed compute info date "+e);
		}
		return infoDate;
	}

	public boolean isComplete( Set<Future<Boolean>> futures){

		for (Future<?> future : futures) {
			if (!future.isDone()) {
				logger.debug("Not all proccess completed...");
				return false;
			}
		}
		logger.debug("All proccess completed...");
		return true;
	}




}
