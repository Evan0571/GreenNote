package com.evan.greennote.auth.config;

import com.evan.framework.common.constant.DateConstants;
import com.evan.framework.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateConstants.DATE_FORMAT_Y_M_D_H_M_S));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateConstants.DATE_FORMAT_Y_M_D_H_M_S));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateConstants.DATE_FORMAT_Y_M_D));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateConstants.DATE_FORMAT_Y_M_D));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateConstants.DATE_FORMAT_H_M_S));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateConstants.DATE_FORMAT_H_M_S));
        javaTimeModule.addSerializer(YearMonth.class, new YearMonthSerializer(DateConstants.DATE_FORMAT_Y_M));
        javaTimeModule.addDeserializer(YearMonth.class, new YearMonthDeserializer(DateConstants.DATE_FORMAT_Y_M));
        objectMapper.registerModule(javaTimeModule);
        JsonUtils.init(objectMapper);
        return objectMapper;
    }
}
