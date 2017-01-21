package ravioli.gravioli.tekkit.database;

import org.bukkit.Bukkit;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.api.machines.PhysicalMachine;
import ravioli.gravioli.tekkit.database.utils.DatabaseUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;

public class Sqlite {
    private Tekkit plugin;
    private Connection connection;

    public Sqlite(Tekkit plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        plugin.getDataFolder().mkdir();
        connection = createConnection();
    }

    private Connection createConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "tekkit.db");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Create a table in the database that will store all physical machines of the given object type
     *
     * @param machine machine to register in the db
     */
    public void createTable(PhysicalMachine machine) {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS `")
                .append(machine.getFormattedName())
                .append("`(id INTEGER PRIMARY KEY AUTOINCREMENT");

        Field[] fields = DatabaseUtils.getAllFields(machine.getClass());
        for (Field field : fields) {
            if (field.getAnnotation(DatabaseObject.class) == null) {
                continue;
            }
            Class type = field.getType();

            String row = "TEXT NOT NULL";
            if (DatabaseUtils.isTypeAssignableFrom(type, Integer.TYPE, Long.TYPE)) {
                row = "INTEGER NOT NULL";
            } else if (DatabaseUtils.isTypeAssignableFrom(type, Double.TYPE, Float.TYPE)) {
                row = "REAL NOT NULL";
            } else if (Number.class.isAssignableFrom(type) || DatabaseUtils.isTypeAssignableFrom(type, Boolean.TYPE)) {
                row = "NUMERIC NOT NULL";
            }
            query.append(", `")
                    .append(field.getName())
                    .append("` ")
                    .append(row);
        }
        query.append(")");

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadMachines(PhysicalMachine machine) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM `" + machine.getFormattedName() + "`")) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String worldName = results.getString("location").split(",")[0];
                if (Bukkit.getWorld(worldName) != null) {
                    machine.load(results);
                } else {
                    plugin.getMachineManager().addUnloadedMachine(worldName, machine);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadMachine(PhysicalMachine machine) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM `" + machine.getFormattedName() + "` WHERE `id` = ?")) {
            statement.setInt(1, machine.getId());

            ResultSet results = statement.executeQuery();
            machine.load(results);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
