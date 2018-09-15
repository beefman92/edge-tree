package com.my.edge.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creator: Beefman
 * Date: 2018/7/26
 */
public class JsonSerializer {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Serializing object " + value + " into json string failed. ", e);
        }
    }
}
