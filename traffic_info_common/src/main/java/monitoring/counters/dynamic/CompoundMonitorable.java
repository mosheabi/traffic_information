package monitoring.counters.dynamic;

import monitoring.IMonitorable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Yegorov
 */
public class CompoundMonitorable<T extends IMonitorable> {

    private Map<Key, monitoring.counters.dynamic.TemplatedMonitorable<T>> data = new HashMap<>();

    private CompoundMonitorable() {
    }

    public monitoring.counters.dynamic.TemplatedMonitorable<T> get(Object... args) {
        return data.get(new Key(args));
    }

    public static class Builder<T extends IMonitorable> {

        private CompoundMonitorable<T> value = new CompoundMonitorable<>();

        public Builder<T> append(TemplatedMonitorable<T> counter, Object... flags) {
            value.data.put(new Key(flags), counter);
            return this;
        }

        public CompoundMonitorable build() {
            return value;
        }

        public Builder<T> extend() {
            Builder<T> extended = new Builder<>();
            extended.value.data.putAll(this.value.data);
            return extended;
        }

    }

    private static class Key {
        private Object[] flags;

        public Key(Object[] flags) {
            this.flags = flags;
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
            return Arrays.equals(flags, other.flags);

        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(flags);
        }
    }

}
