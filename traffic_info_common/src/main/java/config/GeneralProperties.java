package config;

import config.properties.Property;
import config.properties.PropertyDefinitions;

public class GeneralProperties implements PropertyDefinitions {

	//import this config.properties definitions
	private static monitoring.MonitoringProperties MonitoringProperties;
	
	public static final Property REG_EX_WARNING_THRESHOLD_IN_MILI = new Property("reg.ex.warning.threshold.in.mili","15000","If a regex takes more than that amount of miliseconds a warning will be written to the log");
	
	public static final Property MQ_MESSAGE_LISTENER_MANAGE_ACKNOALEGEMENT = new Property("mq.message.listener.manage.acknoalegement","false","If true, messages will be acknowlaged  to MQ after their processing was finished, instead of when fetching");
	
	public static final Property MQ_NUMBER_OF_MESSAGES_TO_COMMIT_EACH_TIME = new Property("mq.number.of.messages.to.commit.each.time","5","how many messages to commit each time mqbase stopps for commit");
	public static final Property MQ_NUMBER_OF_MESSAGES_IN_COMMIT_QUEUE_TO_FORCE_COMMIT_QUE_CLEANUP = new Property("mq.number.of.messages.in.commit.queue.to.force.commit.que.cleanup","10","how many messages to can accumulate in commit/rollback queue before we stop and aggresively commit them");
	
	public static Property EXIT_AFTER_HANDLING_K_MESSAGES = new Property("exit.after.handling.k.messages","0","Default should always be 0. Should be used for tuning purposes only. If 0 this flag has no effect. If the value is bigger that 0 - process will exit after commiting that amount of messaeges");
}
