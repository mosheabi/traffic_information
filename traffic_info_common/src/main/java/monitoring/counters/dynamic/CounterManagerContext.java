package monitoring.counters.dynamic;

/**
 * @author Alexander Yegorov
 */
public class CounterManagerContext {

    private static final ThreadLocal<CounterManager> INSTANCE = new ThreadLocal<>();

    public static void attach(CounterManager instance) {
        INSTANCE.set(instance);
    }

    public static void detach() {
        INSTANCE.remove();
    }

    public static CounterManager get() {
        return INSTANCE.get();
    }

}
