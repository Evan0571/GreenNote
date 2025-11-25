package com.evan.greennote.user.biz.util;

import cn.hutool.core.lang.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class JsonUtils {
     private static ObjectMapper OBJECT_MAPPER=new ObjectMapper();
     static {
         OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         OBJECT_MAPPER.registerModules(new JavaTimeModule());
    }
     public static void init(ObjectMapper objectMapper){
         OBJECT_MAPPER=objectMapper;
    }
     @SneakyThrows
     public static String toJsonString(Object obj){
         return OBJECT_MAPPER.writeValueAsString(obj);
     }

     //Json字符串转化为对象
    @SneakyThrows
    public static <T> T parseObject(String jsonStr, Class<T> clazz){
         if(StringUtils.isBlank(jsonStr)){
             return null;
         }
         return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    //Json字符串转Map
    public static<K,V> Map<K,V> parseMap(String jsonStr, Class<K> keyClass, Class<V> valueClass) throws Exception{
        //创建TypeReference，指定泛型类型
        TypeReference<Map<K,V>> typeRef=new TypeReference<Map<K,V>>() {
        };
        //将JSON字符串转为MAP
        return OBJECT_MAPPER.readValue(jsonStr, OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
    }

    //Json字符串转List
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
