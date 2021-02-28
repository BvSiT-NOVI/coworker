package nl.bvsit.coworker.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;

public class LogUtil {
    public static void logMappedObject(Object obj, Logger logger){
        logMappedObject( obj, logger,false,false);
    }

    public static void logMappedObject(Object obj, Logger logger,boolean indent){
        logMappedObject( obj, logger,indent,true);
    }

    public static void logMappedObject(Object obj, Logger logger,boolean indent,boolean includeClassName){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (indent ) objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            if (includeClassName) logger.info(obj.getClass().getCanonicalName());
            logger.info(objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
