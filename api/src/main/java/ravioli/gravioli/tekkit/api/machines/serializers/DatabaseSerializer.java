package ravioli.gravioli.tekkit.api.machines.serializers;

public interface DatabaseSerializer<T> {
    String serialize(T object);
    T deserialize(String object);
}
