package monitoring.counters.dynamic;

import monitoring.IMonitorable;
import monitoring.counters.dynamic.creator.MonitorableCreator;
import org.apache.commons.lang3.concurrent.AtomicSafeInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentException;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TemplatedMonitorable<T extends IMonitorable> {

    private final ConcurrentMap<Key, Initializer<T>> INITIALIZERS = new ConcurrentHashMap<>();

    private String template;

    private String description;

    private MonitorableCreator<T> creator;

    public TemplatedMonitorable(String template, String description, MonitorableCreator<T> creator) {
        this.template = template;
        this.description = description;
        this.creator = creator;
    }

    public T get(Object... args) {
        try {
            Key key = new Key(args);
            Initializer<T> initializer = INITIALIZERS.putIfAbsent(key, new Initializer<>(this, key));
            if (initializer == null) {
                initializer = INITIALIZERS.get(key);
            }
            return initializer.get();
        } catch (ConcurrentException e) {
            throw new IllegalStateException("Failed to get counters.", e);
        }
    }

    private static class Key {
        private Object[] data;

        public Key(Object[] data) {
            this.data = data != null ? data : new Object[0];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key other = (Key) o;
            return Arrays.equals(data, other.data);

        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }

    private static class Initializer<T extends IMonitorable> extends AtomicSafeInitializer<T> {
        private TemplatedMonitorable<T> holder;
        private Key key;

        public Initializer(TemplatedMonitorable<T> holder, Key key) {
            this.holder = holder;
            this.key = key;
        }

        @Override
        protected T initialize() throws ConcurrentException {
            String name = TemplateFormatter.format(holder.template, key.data);
            return holder.creator.create(name, holder.description);
        }
    }

}
