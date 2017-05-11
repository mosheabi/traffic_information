package config.properties;

public class BasicProperties implements PropertyDefinitions {
    public static Property IN_MQ_THREADS_NUM_FACTOR = new Property("in.mq.multithreaded.threads.num.factor", "", "factor to use when calculating in mq threads number", true);
}
