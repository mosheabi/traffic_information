package monitoring.utils;

public class MonitoringException extends Exception {

	public MonitoringException() {
		super();
		
	}

	public MonitoringException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public MonitoringException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public MonitoringException(String message) {
		super(message);
		
	}

	public MonitoringException(Throwable cause) {
		super(cause);
		
	}
	

}
