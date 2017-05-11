package config.notifications;

public interface StopEventListener extends BisListener {
	
	public void onStopEvent(Object eventObject);

}
