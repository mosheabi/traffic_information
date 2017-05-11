package monitoring.counters.dynamic.creator;

import monitoring.counters.SimpleIntegerCounter;


public class CounterCreator extends AbstractMonitorableCreator<SimpleIntegerCounter, CounterCreator.CounterConfig> {

    public static final CounterConfig DEFAULT_CONFIG = new CounterConfig(0L, true);

    public CounterCreator() {
    }

    public CounterCreator(CounterConfig config) {
        super(config);
    }

    @Override
    public SimpleIntegerCounter create(String name, String description) {
        CounterConfig config = getConfig();
        if (config == null) {
            config = DEFAULT_CONFIG;
        }
        return new SimpleIntegerCounter(name, config.defaultValue, config.initAfterEachPublish, description);
    }

    public static class CounterConfig {
        private long defaultValue;
        private boolean initAfterEachPublish;

        public CounterConfig(long defaultValue, boolean initAfterEachPublish) {
            this.defaultValue = defaultValue;
            this.initAfterEachPublish = initAfterEachPublish;
        }
    }

}
