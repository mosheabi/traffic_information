package monitoring.counters.dynamic.creator;

import monitoring.IMonitorable;
import monitoring.counters.dynamic.creator.MonitorableCreator;

/**
 * @author Alexander Yegorov
 */
public abstract class AbstractMonitorableCreator<T extends IMonitorable, C> implements MonitorableCreator<T> {

    private C config;

    public AbstractMonitorableCreator() {
        this(null);
    }

    public AbstractMonitorableCreator(C config) {
        this.config = config;
    }

    protected C getConfig() {
        return config;
    }

}
