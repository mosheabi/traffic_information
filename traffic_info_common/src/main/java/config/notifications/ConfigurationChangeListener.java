package config.notifications;

public interface ConfigurationChangeListener extends BisListener {

	public abstract void onConfigurationChanged(Object eventObject);

}