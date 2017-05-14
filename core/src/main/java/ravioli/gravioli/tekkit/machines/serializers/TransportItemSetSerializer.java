package ravioli.gravioli.tekkit.machines.serializers;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import ravioli.gravioli.tekkit.api.machines.serializers.DatabaseSerializer;
import ravioli.gravioli.tekkit.machines.transport.TransportItem;
import ravioli.gravioli.tekkit.machines.transport.TransportItemSet;
import ravioli.gravioli.tekkit.machines.utils.MachineUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TransportItemSetSerializer implements DatabaseSerializer<TransportItemSet> {
    @Override
    public String serialize(TransportItemSet object) {
        String[] items = new String[object.size()];

        Iterator<TransportItem> iterator = object.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            TransportItem item = iterator.next();

            Map<String, String> data = new HashMap();

            data.put("item", MachineUtils.itemStackArrayToBase64(item.getItemStack()));
            data.put("location", MachineUtils.locationToString(item.getLocation()));
            data.put("input", item.input.toString());
            data.put("output", item.output == null? "null" : item.output.toString());
            data.put("reachedCenter", item.reachedCenter + "");

            items[index] = new Gson().toJson(data);
            index++;
        }
        return StringUtils.join(items, "\\|");
    }

    @Override
    public TransportItemSet deserialize(String object) {
        String[] items = object.split("\\|");

        TransportItemSet set = new TransportItemSet();
        for (int i = 0; i < items.length; i++) {
            set.add(TransportItem.deserialize(items[i]));
        }
        return set;
    }
}
