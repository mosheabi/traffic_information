package monitoring.counters.dynamic.creator;

import monitoring.counters.Timer;

/**
 * @author Alexander Yegorov
 */
public class TimerCreator extends AbstractMonitorableCreator<Timer, Void> {

    public TimerCreator() {
    }

    @Override
    public Timer create(String name, String description) {
        return new Timer(name, description);
    }
}
