package monitoring.counters.dynamic.creator;

import monitoring.IMonitorable;

/**
 * @author Alexander Yegorov
 */
public interface MonitorableCreator<T extends IMonitorable> {

    T create(String name, String description);

}
