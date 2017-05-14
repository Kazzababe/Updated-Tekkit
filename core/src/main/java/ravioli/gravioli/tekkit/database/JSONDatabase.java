package ravioli.gravioli.tekkit.database;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class JSONDatabase {
    private static String FILE_NAME;
    private JSONObject jsonObject = new JSONObject();
    private Tekkit plugin;

    public JSONDatabase(Tekkit plugin) {
        this.plugin = plugin;

        FILE_NAME = plugin.getDataFolder().getAbsolutePath() + File.separator + "tekkit.json";
        try {
            loadFile();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void loadFile() throws IOException, ParseException {
        jsonObject = (JSONObject) new JSONParser().parse(new FileReader(FILE_NAME));
        System.out.println(jsonObject);
    }

    public void loadMachines(PhysicalMachine machine) {
        JSONArray array = ((JSONArray) ((JSONObject) jsonObject.get(machine.getFormattedName()))
                .get("machines"));

        Iterator<JSONObject> iterator = array.iterator();
        while (iterator.hasNext()) {
            JSONObject object = iterator.next();
            String worldName = ((String) object.get("location")).split(",")[0];

            try {
                if (Bukkit.getWorld(worldName) != null) {
                    //machine.load(object);
                } else {
                    plugin.getMachineManager().addUnloadedMachine(worldName, machine);
                }
            } catch (KeyAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
    }
    public void loadMachine(PhysicalMachine machine) {
        JSONArray array = ((JSONArray) ((JSONObject) jsonObject.get(machine.getFormattedName()))
                .get("machines"));

        Iterator<JSONObject> iterator = array.iterator();
        while (iterator.hasNext()) {
            JSONObject object = iterator.next();
            if((int) object.get("id") == machine.getId()) {
                //machine.load(object);
            }
        }
    }
}
