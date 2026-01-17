package com.evan.framework.common.util;

import cn.hutool.core.lang.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 全局的时间格式：2025-11-21 01:03:08
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 配置 Java8 时间模块，指定 LocalDateTime 的序列化/反序列化格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DATETIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DATETIME_FORMATTER));
        OBJECT_MAPPER.registerModule(javaTimeModule);

        // 不要用时间戳，而是用字符串
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void init(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @SneakyThrows
    public static String toJsonString(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    // Json字符串转化为对象
    @SneakyThrows
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    // Json字符串转Map
    public static <K, V> Map<K, V> parseMap(String jsonStr,
                                            Class<K> keyClass,
                                            Class<V> valueClass) throws Exception {
        TypeReference<Map<K, V>> typeRef = new TypeReference<Map<K, V>>() {};
        return OBJECT_MAPPER.readValue(
                jsonStr,
                OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass)
        );
    }

    // Json字符串转List
    @SneakyThrows
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        CollectionType listType = OBJECT_MAPPER.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return OBJECT_MAPPER.readValue(jsonStr, listType);
    }
}
