package bloomberg.presto.accumulo.iterators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.util.Pair;

public class TestKeyValueIterator
        implements SortedKeyValueIterator<Key, Value> {

    private List<Pair<Key, Value>> keyValues = new ArrayList<>();
    private Iterator<Pair<Key, Value>> iter = null;
    private Pair<Key, Value> currKVP;

    public TestKeyValueIterator() {
    }

    public TestKeyValueIterator(Key k, Value v) {
        keyValues.add(new Pair<Key, Value>(k, v));
    }

    public TestKeyValueIterator add(Key k, Value v) {
        keyValues.add(new Pair<Key, Value>(k, v));
        return this;
    }

    public TestKeyValueIterator addAll(List<Pair<Key, Value>> kvps) {
        keyValues.addAll(kvps);
        return this;
    }

    public TestKeyValueIterator clear() {
        keyValues.clear();
        iter = null;
        return this;
    }

    @Override
    public void init(SortedKeyValueIterator<Key, Value> source,
            Map<String, String> options, IteratorEnvironment env)
                    throws IOException {
    }

    @Override
    public boolean hasTop() {
        if (iter == null) {
            Collections.sort(keyValues, new Comparator<Pair<Key, Value>>() {

                @Override
                public int compare(Pair<Key, Value> o1, Pair<Key, Value> o2) {
                    return o1.getFirst().compareTo(o2.getFirst());
                }
            });

            iter = keyValues.iterator();
            if (iter.hasNext()) {
                currKVP = iter.next();
                return true;
            } else {
                return false;
            }
        }

        if (currKVP != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void next() throws IOException {
        if (iter.hasNext()) {
            currKVP = iter.next();
        } else {
            currKVP = null;
        }
    }

    @Override
    public void seek(Range range, Collection<ByteSequence> columnFamilies,
            boolean inclusive) throws IOException {
        if (range.isInfiniteStartKey()) {
            iter = null;
        } else {
            throw new UnsupportedOperationException(
                    "Can only seek to beginning of list (infinite start key)");
        }
    }

    @Override
    public Key getTopKey() {
        return currKVP.getFirst();
    }

    @Override
    public Value getTopValue() {
        return currKVP.getSecond();
    }

    @Override
    public SortedKeyValueIterator<Key, Value> deepCopy(
            IteratorEnvironment env) {
        TestKeyValueIterator copy = new TestKeyValueIterator();
        copy.addAll(keyValues);
        return copy;
    }

}