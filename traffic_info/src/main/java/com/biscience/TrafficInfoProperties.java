package com.biscience;

import config.properties.Property;
import config.properties.PropertyDefinitions;
import constants.TrafficInfoCmdParams;

/**
 * Created by Anna Kuranda on 5/1/2017.
 */
public class TrafficInfoProperties implements PropertyDefinitions {

    public static final Property QUERY_TO_RUN = new Property(System.getProperty(TrafficInfoCmdParams.QUERY_KEY), "","QUERY TO RUN");

    public static final Property SW_TOKEN = new Property("explorer.similarweb.token", "083d69224dcb8303f0b2638e272356af","similarweb token");

    public static final Property SOCKET_TIMEOUT = new Property("socket.timeout","30","socket timeout for http connection.Prevent freeze of site");
    public static final Property CSV_SEPARATOR = new Property("csv.separator","\t","csv separator");
    public static final Property NUM_PROCESSES = new Property("publishers.threads.num","4","number of publishers run  ");

    public static final Property MAIL_SMTP_HOST = new Property("mail.smtp.host","localhost","SMTP HOST  ");

    public static final Property MAIL_FROM = new Property("mail.from","    mail.from=anna.kuranda@biscience.com","MAIL SENDER  ");

    public static final Property MAIL_TO = new Property("mail.to","anna.kuranda@biscience.com,moshe.abraham@biscience.com","MAIL RECEPIENTS  ");
    public static final Property MAIL_SMTP_PORT = new Property("mail.smtp.port","25","MAIL SMTP PORT  ");


    public static final Property MAIL_PASS = new Property("mail.pass","svC9ZnppW1mR","MAIL PASS  ");

    public static final Property MAIL_USER = new Property("mail.user","alerts@biscience.com","MAIL USER  ");

    public static final Property MAIL_MSG = new Property("mail.message","upgrade you account","MAIL MESSAGE  ");

    public static final Property MAIL_SUBJECT = new Property("mail.subject","upgrade you account","MAIL MESSAGE  ");




//    public static final Property DAYS_NOT_UPDATED_SW_INFO = new Property("explorer.days.not.updated.sw.info", "0","num of days allowed since last sw update (min num of days need to pass before a sw info update is allowed)");

}
