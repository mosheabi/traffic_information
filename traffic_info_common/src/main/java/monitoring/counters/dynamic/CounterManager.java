package monitoring.counters.dynamic;

import monitoring.counters.SimpleIntegerCounter;
import monitoring.counters.Timer;
import monitoring.counters.dynamic.CompoundMonitorable;
import monitoring.counters.dynamic.TemplatedMonitorable;
import monitoring.counters.dynamic.creator.CounterCreator;
import monitoring.counters.dynamic.creator.TimerCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Yegorov
 */
public class CounterManager {

    private static final monitoring.counters.dynamic.CompoundMonitorable<SimpleIntegerCounter> counters;
    private static final monitoring.counters.dynamic.CompoundMonitorable<Timer> timers;

    //TODO: rework usage in Javex, check what is the problem to let it stay as Singleton
    private static CounterManager counterManager = null;

    static {
        CounterCreator counterCreator = new CounterCreator();

        monitoring.counters.dynamic.CompoundMonitorable.Builder<SimpleIntegerCounter> builder = new monitoring.counters.dynamic.CompoundMonitorable.Builder<>();
        builder.append(new TemplatedMonitorable<>("${%.:}${%}", "Queue counter by type", counterCreator), By.NONE);
        builder.append(new TemplatedMonitorable<>("Queue.${%.:}${%}.${%}", "Queue counter by country/type", counterCreator), By.COUNTRY);
        builder.append(new TemplatedMonitorable<>("Channel.${%.:}${%}.${%}", "Queue counter by channel/type", counterCreator), By.CHANNEL);
        builder.append(new TemplatedMonitorable<>("Queue.${%.:}${%}.Channel.${%}.${%}", "Queue counter by country/channel/type", counterCreator), By.COUNTRY, By.CHANNEL);
        counters = builder.build();

        TimerCreator timerCreator = new TimerCreator();
        monitoring.counters.dynamic.CompoundMonitorable.Builder<Timer> timersBuilder = new CompoundMonitorable.Builder<>();
        timersBuilder.append(new TemplatedMonitorable<>("${%.:}${%}", "Queue counter by type", timerCreator), By.NONE);
        timers = timersBuilder.build();
    }

    private final String QUEUE_TYPE;

    private Integer channel;

    private String country;

    public static CounterManager getInstance () {
        synchronized (CounterManager.class) {
            if (counterManager == null)
                counterManager = new CounterManager();
        }
        return counterManager;
    }

    public static CounterManager getInstance (String queueType) {
        synchronized (CounterManager.class) {
            if (counterManager == null)
                counterManager = new CounterManager(queueType);
        }
        return counterManager;
    }

    public CounterManager() {
        this(null);
    }

    public CounterManager(String queueType) {
        this.QUEUE_TYPE = queueType;
    }

    public com.codahale.metrics.Timer.Context getTimer(String type) {
        Timer timer = timers.get(By.NONE).get(QUEUE_TYPE, type);
        return timer.isEnabled() ? timer.getTimerCntext() : null;
    }

    public void set(Integer channel) {
        this.channel = channel;
    }

    public void set(String country) {
        this.country = country;
    }

    public void set(Integer channel, String country) {
        this.channel = channel;
        this.country = country;
    }

    public void clear() {
        this.channel = null;
        this.country = null;
    }

    public void inc(Object type, int delta) {
        for (int i = 0; i < delta; i++) {
            inc(type);
        }
    }

    public void inc(Object type) {
        modify(type, 1L);
    }

    public void dec(Object type) {
        modify(type, -1L);
    }

    public void modify(Object type, long delta) {
        List<SimpleIntegerCounter> counters = getCounters(type);
        for (SimpleIntegerCounter counter : counters) {
            counter.addAndGet(delta);
        }
    }

    private List<SimpleIntegerCounter> getCounters(Object type) {
        List<SimpleIntegerCounter> counters = new ArrayList<>();
        counters.add(CounterManager.counters.get(By.NONE).get(QUEUE_TYPE, type));
        if (country != null) {
            counters.add(CounterManager.counters.get(By.COUNTRY).get(QUEUE_TYPE, country, type));
        }
        if (channel != null) {
            counters.add(CounterManager.counters.get(By.CHANNEL).get(QUEUE_TYPE, channel, type));
        }
        if (country != null && channel != null) {
            counters.add(CounterManager.counters.get(By.COUNTRY, By.CHANNEL).get(QUEUE_TYPE, country, channel, type));
        }
        return counters;
    }

    private enum By {
        NONE,
        COUNTRY,
        CHANNEL
    }

    public enum Types {
        RECEIVED("ticketsReceived"),
        COMPLETED("ticketsCompleted"),
        FAILED("ticketsFailed");

        private String value;

        Types(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
