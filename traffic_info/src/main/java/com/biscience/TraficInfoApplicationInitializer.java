package com.biscience;

import com.biscience.service.TrafficInfoService;
import config.ConfigurationManager;
import constants.TrafficInfoCmdParams;
import db.DbProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@SpringBootApplication
public class TraficInfoApplicationInitializer implements CommandLineRunner,Runnable {
    private static Logger logger = Logger.getLogger(TraficInfoApplicationInitializer.class);


    @Autowired
    private TrafficInfoService trafficInfoService;

    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(TraficInfoApplicationInitializer.class);
        application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
        SpringApplication.run(TraficInfoApplicationInitializer.class,args);


    }

    @Override
    public void run(String... args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: java -jar .jar -p <config.properties-file-name> -processId <processId> -query_key <run query>" );
            System.exit(1);
        }

        try {
            ConfigurationManager.init("TrafficInfo", args, DbProperties.class, null);
            ConfigurationManager.init("TrafficInfo", args,TrafficInfoProperties.class, null);
            if(StringUtils.isEmpty(System.getProperty(TrafficInfoCmdParams.QUERY_KEY))){
                System.err.println("Please define -query_key properly");
                System.exit(1);
            }
            if(StringUtils.isEmpty( TrafficInfoProperties.QUERY_TO_RUN.getValue())){
                System.err.println("The query that get publishers for SW undefined ");
                System.exit(1);
            }
            System.out.println("Should execute query name " + System.getProperty(TrafficInfoCmdParams.QUERY_KEY));
            System.out.println("Should execute query " + TrafficInfoProperties.QUERY_TO_RUN.getValue());
            this.run();

        }catch(Exception e){
            logger.error("Unhandled exception", e);
            System.exit(1);
        }

        //log.info("Look in application.yml for the  directory path. Files will be created every 30 seconds.");
    }


    @Override
    public void run() {
        trafficInfoService.init();
        trafficInfoService.execute();
    }
}
