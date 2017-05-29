package monitoring;

import config.properties.Property;
import config.properties.PropertyDefinitions;

public class MonitoringProperties implements PropertyDefinitions {
	

	/**
	 * Note that the actuall names of some of the config.properties are used in jvm-sun-hotspot.json (under../jmx package) -
	 * Do not change the names
	 */
	public static final Property STATD_IS_ACTIVE = new Property("monitoring.is.active","true","Is the mechanism to publish monitoring counters active");
	public static final Property STATD_HOST_PROPERTY_KEY_NAME = new Property("bis.monitor.host.ip","${bis_monitor_ip}:173.45.126.74","Ip of the statd server");//In dev - 173.45.126.74 In prod -10.132.130.235
	public static final Property STATD_PORT_PROPERTY_KEY_NAME = new Property("bis.monitor.port.number","${bis_monitor_port}:2003","port of the statd server");//2003
	public static final Property STATD_PUBLISH_TIME_PROPERTY_KEY_NAME = new Property("monitoring.publish.delay.in.mili","5000","interval of publishing counters to mnitor (in miliseconds)");
	public static final Property STATD_IS_DEBUG_PUBLISHER = new Property("monitoring.use.debug.implementation.instead.of.publisher","false","Should DEBUG mode be used - publishing to log4j instead of monitor server.");
	
	public static final Property JMXTRANS_QUERYINTERVALINSECONDS = new Property("jmxtrans.queryIntervalInSeconds","30","jmxtrans (mechanism to publish jvm JMX to monitor) queryIntervalInSeconds");
	public static final Property JMXTRANS_NUMQUERYTHREADS = new Property("jmxtrans.numQueryThreads","1","jmxtrans (mechanism to publish jvm JMX to monitor) numQueryThreads");
	public static final Property JMXTRANS_NUMEXPORTTHREADS = new Property("jmxtrans.numExportThreads","1","jmxtrans (mechanism to publish jvm JMX to monitor) numExportThreads");
	public static final Property JMXTRANS_EXPORTINTERVALINSECONDS = new Property("jmxtrans.exportIntervalInSeconds","5","jmxtrans (mechanism to publish jvm JMX to monitor) exportIntervalInSeconds");
	public static final Property JMXTRANS_EXPORTBATCHSIZE = new Property("jmxtrans.exportBatchSize","50","jmxtrans (mechanism to publish jvm JMX to monitor) exportBatchSize");
	
	public static final Property ENABLE_META_REFRESH = new Property("shared.enable.meta.refresh","true","Enable Meta Refresh");


}
