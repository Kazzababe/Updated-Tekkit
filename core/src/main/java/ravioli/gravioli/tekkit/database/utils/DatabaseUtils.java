package ravioli.gravioli.tekkit.database.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseUtils {
    public static Field[] getAllFields(Class clazz) {
        Set<Field> fields = new HashSet();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public static boolean isTypeAssignableFrom(Class type, Class... toTest) {
        for (Class test : toTest) {
            if (!type.isAssignableFrom(test)) {
                return false;
            }
        }
        return true;
    }
}
