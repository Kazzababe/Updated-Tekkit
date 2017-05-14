package ravioli.gravioli.tekkit.machines.transport;

import com.google.common.collect.Sets;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class TransportItemSet extends AbstractSet<TransportItem> {
    private Set<TransportItem> items = Sets.newConcurrentHashSet();

    @Override
    public Iterator<TransportItem> iterator() {
        return items.iterator();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean add(TransportItem item) {
        return items.add(item);
    }

    @Override
    public boolean remove(Object object) {
        return items.remove(object);
    }
}
