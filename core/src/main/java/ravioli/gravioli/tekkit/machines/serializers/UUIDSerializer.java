package ravioli.gravioli.tekkit.machines.serializers;

import ravioli.gravioli.tekkit.api.machines.serializers.DatabaseSerializer;

import java.util.UUID;

public class UUIDSerializer implements DatabaseSerializer<UUID> {
    @Override
    public String serialize(UUID object) {
        return object.toString();
    }

    @Override
    public UUID deserialize(String object) {
        return UUID.fromString(object);
    }
}