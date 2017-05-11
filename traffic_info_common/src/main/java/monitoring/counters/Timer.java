package monitoring.counters;

import config.ConfigurationManager;
import monitoring.IMonitorable;
import monitoring.MonitoringManager;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class Timer implements IMonitorable {
	
	private String counterName;
	
	private String desciption;
	
	private static Logger log = Logger.getLogger(ConfigurationManager.class);
	private Logger log4JLogger ;

	private com.codahale.metrics.Timer internalMetricObject = null;
	

	public Timer(String counterName, String desciption) {
		super();
		this.counterName = counterName;
		this.desciption = desciption;
		this.log4JLogger = Logger.getLogger(IMonitorable.LOG_4_J_NAME_EXTENSION + counterName );
		
		
		MonitoringManager.getInstance().registerCounter(this);
	
	}

	@Override
	public String getCounterName() {
		
		return counterName;
	}

	@Override
	public String getCounterDescription() {
		
		return desciption;
	}

	@Override
	public boolean isEnabled() {
		
		return log4JLogger.isEnabledFor(Level.WARN);
	}
	
	

	@Override
	public Metric getMetricInternalObect() {
		
		return internalMetricObject;
	}

	@Override
	public void createMetricInternalObjet(MetricRegistry metrics, String key) {
		internalMetricObject = metrics.timer(key);
		
	}

	/**
	 * returns and initialized timer contex tostart time measurement
	 * do Context.stop() to terminate counting
	 * @return
	 */
	public com.codahale.metrics.Timer.Context getTimerCntext()
	{
		return this.internalMetricObject.time();
	}
	

}
