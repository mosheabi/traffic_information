package monitoring;

import com.timgroup.statsd.StatsDClient;


/**
 * Visitor pattern
 * 
 * Since every countre implementation knows best how to publish itself - the StatdClient will visit the coter -  so the countre will publish and manage internal state accordingly
 * Every implementation of that interface must register to StatdClient in it's constructor
 * It is better to define all counters as static members of a class implementing ICounterDefinitions And register that class on startup when initializing the Statd mechanism 
 * Otherwise - counter will be added on creation
 * @author alon.malki
 *
 */
public interface ICounter<T> extends IMonitorable {
	
	/**
	 *  Statd client will visit this method. Implementation should use the client to publish the counter value
	 *  We assume this will always happen in a single thread (of the Statd manager)
	 * @param publishKey the key to publish with (include app namespace etc)
	 * @param client  if client is null -  behave the same , just do not send to the client (can be used by getValue() as simple implementation)
	 * @return
	 */
	public T visitStatdClient(String publishKey, StatsDClient client);
	


	/**
	 * retuns the gaugeValue - can behave similar to visitStatdClient , but in this case - it should also retunr the value instead of sending it
	 * @return
	 */
	public T getValue();
	
	
}
