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
    public static final Property CSV_SEPARATOR = new Property("csv.separator",",","csv separator");
    public static final Property NUM_PROCESSES = new Property("num.publishers","4","number of publishers run  ");



//    public static final Property DAYS_NOT_UPDATED_SW_INFO = new Property("explorer.days.not.updated.sw.info", "0","num of days allowed since last sw update (min num of days need to pass before a sw info update is allowed)");

}
