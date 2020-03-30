package com.nivelle.base.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class GsonUtils {

    private static Gson gson = null;
    private static Gson gsonFormat = null;

    static {
        gson = new Gson();
        gsonFormat = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    private GsonUtils() {
    }

    /**
     * 将对象转换成json格式
     *
     * @param ts
     * @return
     */
    public static String toJson(Object ts) {
        String jsonStr = null;
        if (gson != null) {
            jsonStr = gson.toJson(ts);
        }
        return jsonStr;
    }

    /**
     * 将对象转换成json格式字符串
     *
     * @param ts   对象
     * @param type 类型
     * @return json格式字符串
     */
    public static String toJson(Object ts, Type type) {
        String jsonStr = null;
        if (gson != null) {
            jsonStr = gson.toJson(ts, type);
        }
        return jsonStr;
    }

    /**
     * 将json转换成bean对象
     *
     * @param jsonStr
     * @return
     */
    public static <T> T fromJson(String jsonStr, Class<T> cl) {
        Object obj = null;
        if (gson != null) {
            obj = gson.fromJson(jsonStr, cl);
            return (T) obj;
        }
        return null;
    }

    /**
     * 将json转换成bean对象
     *
     * @param jsonStr
     * @return
     */
    public static <T> T fromJsonFormat(String jsonStr, Class<T> cl) {
        Object obj = null;
        if (gsonFormat != null) {
            obj = gsonFormat.fromJson(jsonStr, cl);
            return (T) obj;
        }
        return null;
    }

    /**
     * 将json转换成bean对象
     *
     * @param jsonStr
     * @return
     */
    public static <T> T fromJson(String jsonStr, Type type) {
        Object obj = null;
        if (gson != null) {
            obj = gson.fromJson(jsonStr, type);
            return (T) obj;
        }
        return null;
    }

    /**
     * JSON 转MAP或LIST集合
     *
     * @param json 标准JSON格式字符串
     * @return Object （主要是MAP与List集合）
     */
    @SuppressWarnings("unchecked")
    public static Object parse(String json) {
        if (json == null) {
            return json;
        }
        JsonElement ele = new com.google.gson.JsonParser().parse(json);
        if (ele.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> set = ((JsonObject) ele).entrySet();
            Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
            HashMap<String, Object> map = new HashMap<>();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive()) {
                    map.put(key, parse(value.toString()));
                } else {
                    map.put(key, value.getAsString());
                }
            }
            return map;
        } else if (ele.isJsonArray()) {
            JsonArray set = ele.getAsJsonArray();
            Iterator<JsonElement> iterator = set.iterator();
            List list = new ArrayList();
            while (iterator.hasNext()) {
                JsonElement entry = iterator.next();
                if (!entry.isJsonPrimitive()) {
                    list.add(parse(entry.toString()));
                } else {
                    list.add(entry.getAsString());
                }
            }
            return list;
        } else if (ele.isJsonPrimitive()) {
            return json;
        }
        return null;
    }

    public static String toFormatDateJson(Object ts) {
        String jsonStr = null;
        if (gsonFormat != null) {
            jsonStr = gsonFormat.toJson(ts);
        }
        return jsonStr;
    }

    public static <T> T fromFormatDateJson(String jsonStr, Type type) {
        Object obj = null;
        if (gsonFormat != null) {
            obj = gsonFormat.fromJson(jsonStr, type);
            return (T) obj;
        }
        return null;
    }


}
