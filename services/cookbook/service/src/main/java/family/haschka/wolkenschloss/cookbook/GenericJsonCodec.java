package family.haschka.wolkenschloss.cookbook;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import javax.json.bind.Jsonb;

public class GenericJsonCodec<T> implements MessageCodec<T, T> {

    private final Jsonb jsonb;
    private final Class<T> clazz;

    GenericJsonCodec(Jsonb jsonb, Class<T> clazz) {
        this.jsonb = jsonb;
        this.clazz = clazz;
    }

    @Override
    public void encodeToWire(Buffer buffer, T t) {
        var json = jsonb.toJson(t);
        buffer.appendString(json);
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        var json = buffer.toString();
        return jsonb.fromJson(json, clazz);
    }

    @Override
    public T transform(T t) {
        return t;
    }

    @Override
    public String name() {
        return clazz.getName() + "Codec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
