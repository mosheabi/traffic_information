package monitoring.jmx;

import config.notifications.StopEventListener;
import monitoring.MonitoringProperties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmxtrans.embedded.EmbeddedJmxTrans;
import org.jmxtrans.embedded.config.ConfigurationParser;

import java.io.InputStream;

public class PushJVMData implements StopEventListener
{
	
	private  EmbeddedJmxTrans jmxTransEngine = null;
	
	/**
	 * uses json configuration to push data 
	 * you can define system config.properties to fine tune ( look for ${jmxtrans.  ..  )
	 * @throws Exception
	 */
	public void start() throws Exception
	{
		//make sure debug messages are logged		
		Logger.getLogger("org.jmxtrans.embedded").setLevel(Level.WARN); //log4j.logger.org.jmxtrans.embedded=WARN
		
		if(!MonitoringProperties.STATD_IS_ACTIVE.getBooleanValue())
		{
			return ;
		}
		
		System.setProperty("bis_jmx_publish_graphite_enabled", !MonitoringProperties.STATD_IS_DEBUG_PUBLISHER.getBooleanValue()+"");
		System.setProperty("bis_jmx_publish_console_enabled", MonitoringProperties.STATD_IS_DEBUG_PUBLISHER.getBooleanValue()+"");
		System.setProperty("bis.monitor.host.ip.jmxtrans", MonitoringProperties.STATD_HOST_PROPERTY_KEY_NAME.getValue());
		System.setProperty("bis.monitor.port.number.jmxtrans", MonitoringProperties.STATD_PORT_PROPERTY_KEY_NAME.getValue());




		InputStream configuration = Thread.currentThread().getContextClassLoader().getResourceAsStream("jvm-sun-hotspot.json");
		jmxTransEngine = new ConfigurationParser().newEmbeddedJmxTrans(configuration);
		configuration.close();
		jmxTransEngine.start();
		
	}

	@Override
	public void onStopEvent(Object eventObject) {
		if(jmxTransEngine != null)
		{
			try {
				jmxTransEngine.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
