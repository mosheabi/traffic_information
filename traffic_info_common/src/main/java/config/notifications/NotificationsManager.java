package config.notifications;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationsManager {
	

	private  Map<String, Set<Object>> listeners = new HashMap<String, Set<Object>>();
	
	public    void addListener(ConfigurationChangeListener listener)
	{		
		addListener(ConfigurationChangeListener.class.getName(), listener);
	}
	
	public   void addListener(StopEventListener listener)
	{		
		addListener(StopEventListener.class.getName(), listener);
	}
	
	public   void removeListener(ConfigurationChangeListener listener)
	{
		removeListener(ConfigurationChangeListener.class.getName(), listener);
		
	}
	public   void removeListener(StopEventListener listener)
	{
		removeListener(StopEventListener.class.getName(), listener);
		
	}
	
	private  synchronized void removeListener(String type,Object listener)
	{
		Set<Object> set = listeners.get(type);
		if(set == null)
		{
			return;		
		}
		set.remove(listener);
	}
	
	
	private  synchronized void addListener(String type,Object listener)
	{
		Set<Object> set = listeners.get(type);
		if(set == null)
		{
			set = new HashSet<Object>();
			listeners.put(type, set);			
		}
		set.add(listener);
	}
	
	
	
	public synchronized void publishConfigurationChange( )
	{
		Set<Object> set = listeners.get(ConfigurationChangeListener.class.getName());
		if(set == null)
		{
			return;		
		}
		
		for(Object l:set)
		{
			( (ConfigurationChangeListener)l).onConfigurationChanged(null);;
		}
	}
	
	public synchronized void publishStopEvent( )
	{
		Set<Object> set = listeners.get(StopEventListener.class.getName());
		if(set == null)
		{
			return;		
		}
		
		for(Object l:set)
		{
			( (StopEventListener)l).onStopEvent(null);
		}
	}
	
}
