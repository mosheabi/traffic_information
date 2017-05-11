package monitoring.counters;


import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.timgroup.statsd.StatsDClient;
import monitoring.ICounter;
import monitoring.IMonitorable;
import monitoring.MonitoringException;
import monitoring.MonitoringManager;
import monitoring.metrics.BisGauge;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleIntegerCounter extends AtomicLong implements ICounter<Long> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long defaultValue;
	private String counterName;
	private boolean initAfterEachPublish;
	private String description;
	private boolean publishOnlyIfValueChanged;
	private long lastValue ;
	private boolean isFirstTime = true;
	private Logger log4JLogger ;
	
	private BisGauge internalObject = null;

	/**
	 * 
	 * @param counterName
	 * @param defaultValue mandatory field
	 * @param initAfterEachPublish if true - value will be initialized back to the default value each time it is published
	
	 * @param description
	 */
	public SimpleIntegerCounter(String counterName,long defaultValue, 
			boolean initAfterEachPublish,  String description
			) {
		super();
		this.defaultValue = defaultValue;
		this.counterName = counterName;
		this.initAfterEachPublish = initAfterEachPublish;
		this.description = description;
		this.publishOnlyIfValueChanged = false;
		
		if(counterName == null || description == null || description.trim().length() == 0 || counterName.trim().length() == 0)
		{
			throw new RuntimeException(new MonitoringException("Counter defined without name or description: name = " + counterName + " , Description = " + description));
		}
		
		if(initAfterEachPublish && publishOnlyIfValueChanged)
		{
			throw new RuntimeException(new MonitoringException("Counter defined with both initAfterEachPublish and initAfterEachPublish equal true: name = " + counterName + " , Description = " + description));
		}
		
		this.set(defaultValue);
		
		this.log4JLogger = Logger.getLogger(IMonitorable.LOG_4_J_NAME_EXTENSION + counterName );
		
		//internalObject= new BisGauge(this);
		
		//StatdClient.getInstance().registerCounter(this);
		MonitoringManager.getInstance().registerCounter(this);
	}

	@Override
	public Long visitStatdClient(String publishKey, StatsDClient client)
	{
		long val = defaultValue;
		
		//if we init every publish then managing isFirstTime and lastValue are not relevant
		if(initAfterEachPublish)
			val = this.getAndSet(defaultValue);
		else
		{
			val=this.get();
			
			if(publishOnlyIfValueChanged)
			{
			
				if(isFirstTime )
				{
					isFirstTime=false;
				}
				else if (val == lastValue)
				{
					//this is the case when we do not publish since the value has not changed from last publish
					return val;
				}
				lastValue = val;
			}
		}
		
		
		//publish
		if(client != null)
		{
			client.recordGaugeDelta(publishKey, val);
		}
		
		return val;
	}

	@Override
	public String getCounterName() {		
		return counterName;
	}

	@Override
	public String getCounterDescription() {
		return description;
	}

	@Override
	public Long getValue() {
		
		return visitStatdClient(null,null);
	}

	@Override
	public boolean isEnabled() {
		
		return log4JLogger.isEnabledFor(Level.WARN);
	}

	@Override
	public Metric getMetricInternalObect() {
		
		return internalObject;
	}

	@Override
	public void createMetricInternalObjet(MetricRegistry metrics, String key) {
		internalObject=  new BisGauge(this);
		
	}


	
	
	

}
