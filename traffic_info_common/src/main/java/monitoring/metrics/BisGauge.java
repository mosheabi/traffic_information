package monitoring.metrics;

import monitoring.ICounter;
import com.codahale.metrics.Gauge;

public class BisGauge<T> implements Gauge<T> {
	
	private ICounter<T> counter = null;
	public BisGauge(ICounter<T> counter)
	{
		this.counter = counter;
	}
	
	public ICounter<T> getInternalCounter()
	{
		return counter;
	}
	
	public  void setInternalCounter(ICounter<? extends Object> counter)
	{
		this.counter = (ICounter<T>)counter;
	}

	@Override
	public T getValue() {
		
		return counter.getValue();
	}
	
	
	

}
