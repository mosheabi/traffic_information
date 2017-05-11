package monitoring;


import config.ConfigurationManager;
import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import config.notifications.StopEventListener;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static monitoring.MonitoringProperties.*;

public class MonitoringManager implements StopEventListener
{
	private static class ShowableMetricFilter implements MetricFilter
	{

		@Override
		public boolean matches(String name, Metric metric) {
			
			IMonitorable m = getInstance().counters.get(name);
			
			return m != null ? m.isEnabled() : false;
		}
		
	}
	
	
	private static Logger log = Logger.getLogger(MonitoringManager.class);
	
	private volatile Map<String, IMonitorable> counters = new HashMap<String, IMonitorable>();
	private String prefix = "";
	private static MonitoringManager _instance = new MonitoringManager();
	
	private ScheduledReporter reporter = null;
	
	public static MonitoringManager getInstance()
	{
		return _instance;
	}
	
	public static final MetricRegistry metrics = new MetricRegistry();
	
	@SafeVarargs
	public final  synchronized void init(  Class<? extends ICounterDefinitions> ... countersClasses) throws MonitoringException
	{
		prefix = ConfigurationManager.getInstance().getProccessIdentifier() + ".";
		if(countersClasses != null)
		{
			//register counter classes
			for(Class<? extends ICounterDefinitions> countersClass: countersClasses)
			{
				registerCounters(countersClass);
			}
		}
		
		//make sure debug messages are logged
		Logger.getLogger("com.codahale.metrics").setLevel(Level.WARN);
		
		if( STATD_IS_ACTIVE.getBooleanValue() )
		{
			startReport();
		}
		
		ConfigurationManager.getNotificationsManager().addListener(this);
		
		
	}
	
	
	private   void startReport() {
		
		if(STATD_IS_DEBUG_PUBLISHER.getBooleanValue())
		{
			reporter = ConsoleReporter.forRegistry(metrics)
		          .convertRatesTo(TimeUnit.SECONDS)
		          .convertDurationsTo(TimeUnit.MILLISECONDS).filter(new ShowableMetricFilter())
		          .build();
		      
		      reporter.start(1, TimeUnit.SECONDS);
		}
		else
		{
			final Graphite graphite = new Graphite(new InetSocketAddress(STATD_HOST_PROPERTY_KEY_NAME.getValue(), STATD_PORT_PROPERTY_KEY_NAME.getIntValue()));//2003
			//final Graphite pickledGraphite = new PickledGraphite(new InetSocketAddress("graphite.example.com", 2004));
			reporter = GraphiteReporter.forRegistry(metrics)
	          .convertRatesTo(TimeUnit.SECONDS)
	          // .filter(MetricFilter.ALL)
	          .convertDurationsTo(TimeUnit.MILLISECONDS).filter(new ShowableMetricFilter() ).build(graphite);

		}
		
		reporter.start(STATD_PUBLISH_TIME_PROPERTY_KEY_NAME.getLongValue(), TimeUnit.MILLISECONDS);
     
	  }
	
	
	
	@Override
	public void onStopEvent(Object eventObject) {
		if(this.reporter != null)
		{
				reporter.stop();
				
		}
		
	}


	@SuppressWarnings("unchecked")
	public void registerCounters(Class<? extends ICounterDefinitions> countersClass ) throws MonitoringException
	{
		Field[] fields =  countersClass.getDeclaredFields();
		if(fields == null)
		{
			return;
		}
		
		
		//register :imported" definitions first - so the order will be clear (imported definitions first)
		for(Field f:fields)
		{
			@SuppressWarnings("rawtypes")
			Class memberClass = (Class)f.getGenericType();
			if( ICounterDefinitions.class.isAssignableFrom(memberClass) )
			{
				registerCounters( (Class<? extends ICounterDefinitions> )memberClass);
			}
		}
		
		//register the counters themselves
		for(Field f:fields)
		{
			@SuppressWarnings("rawtypes")
			Class memberClass = (Class)f.getGenericType();
			
			if( IMonitorable.class.isAssignableFrom(memberClass) )
			{
				
				IMonitorable p=null;
				try {
					p = ((IMonitorable)f.get(null));
					this.registerCounter( p);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new MonitoringException("Could not register counter", e);
				}
				
				
			}
			
		}
		
	}
	
	/**
	 * TODO  make sure the constructor of counters calls here too
	 * @param counter
	 */
	@SuppressWarnings("unchecked")
	public synchronized void registerCounter( IMonitorable counter)// ICounter<? extends Object> counter)
	{
		String key = prefix + counter.getCounterName();
		
		IMonitorable existingCounter = counters.get(key);
		
		if(existingCounter != null  )
		{
			if(existingCounter == counter)
			{
				//already registered
				return;
			}
			else
			{
				log.warn("MonitoringManager registration: Counter with key " + counter.getCounterName() + " is overridden");	
							
			}
		}
		else
		{
			if(log.isInfoEnabled())
			{
				log.info("MonitoringManager registered Counter named " + counter.getCounterName() + " with description: " + counter.getCounterDescription() );
				log.info("Use:  log4j.logger.monitoring." +  counter.getCounterName() + "=ERROR  to disable counter");
			}
		}
		
		
		
		//register the new counter
		metrics.remove(key);
		counter.createMetricInternalObjet(metrics, key);
		metrics.remove(key);
		metrics.register(key, counter.getMetricInternalObect());
		
		counters.put(key, counter);
		if(log.isInfoEnabled()){
			log.info("Added counter : |"+counter.getCounterName()+"| in the following location : |"+key+"| , description : "+counter.getCounterDescription());
		}
	}
	

	

}
