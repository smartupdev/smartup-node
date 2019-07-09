package global.smartup.node.util;

import java.util.HashMap;

public class MapBuilder<K,V> {

    private HashMap<K, V> map = new HashMap<>();

    public static <K,V> MapBuilder<K,V> create() {
        return new MapBuilder<>();
    }

    public MapBuilder<K,V> put(K k, V v) {
        map.put(k, v);
        return this;
    }

    public HashMap<K,V> build() {
        return map;
    }

}
