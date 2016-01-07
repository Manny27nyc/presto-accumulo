package bloomberg.presto.accumulo.iterators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.iterators.user.RowFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractBooleanFilter extends RowFilter {

    private static final String FILTER_JAVA_CLASS_NAME = "abstract.boolean.filter.java.class.name";

    protected List<RowFilter> filters = new ArrayList<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void init(SortedKeyValueIterator<Key, Value> source,
            Map<String, String> options, IteratorEnvironment env)
                    throws IOException {
        super.init(source, options, env);
        for (Entry<String, String> e : options.entrySet()) {
            try {
                Map<String, String> props = OBJECT_MAPPER.readValue(
                        e.getValue(), new TypeReference<Map<String, String>>() {
                        });
                String clazz = props.remove(FILTER_JAVA_CLASS_NAME);
                RowFilter f = (RowFilter) Class.forName(clazz).newInstance();
                f.init(this, props, env);
                filters.add(f);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Failed to deserialize Filter information from JSON value "
                                + e.getValue(),
                        ex);
            }
        }
    }

    protected static IteratorSetting combineFilters(
            Class<? extends AbstractBooleanFilter> clazz, int priority,
            IteratorSetting... configs) {
        if (configs == null || configs.length == 0) {
            throw new IllegalArgumentException(
                    "Iterator configs are null or empty");
        }

        Map<String, String> props = new HashMap<>();
        for (IteratorSetting cfg : configs) {
            if (props.containsKey(cfg.getName())) {
                throw new IllegalArgumentException(
                        "Destination config already has config for filter called "
                                + cfg.getName());
            }

            Map<String, String> propCopy = new HashMap<>(cfg.getOptions());
            propCopy.put(FILTER_JAVA_CLASS_NAME, cfg.getIteratorClass());

            try {
                props.put(cfg.getName(),
                        OBJECT_MAPPER.writeValueAsString(propCopy));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(
                        "Failed to encode map as json string", e);
            }
        }

        return new IteratorSetting(priority, UUID.randomUUID().toString(),
                clazz, props);
    }
}