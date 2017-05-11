package monitoring;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

public interface IMonitorable {
	
	
	public static final String LOG_4_J_NAME_EXTENSION = "monitoring.";

	/**
	 * returns the simpel counter name , without any namespace etc
	 * @return
	 */
	public String getCounterName();
	
	/**
	 * returns the desciption - will be used by the StatdClient manager to log registration
	 * @return
	 */
	public String getCounterDescription();

	
	
	
	/**
	 * IS this counter enabled in log4j configuration?
	 * an checked n the code to avoid setting the counter if not needed
	 * Will n be published any way
	 * @return
	 */
	public boolean isEnabled();
	
	
	/**
	 * returns the internal object for monitoring
	 * @return
	 */
	public Metric getMetricInternalObect();
	
	/**
	 * Set the object

	 */
	public void createMetricInternalObjet(MetricRegistry metrics, String key);
}
