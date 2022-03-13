package com.example.feign;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class FeignCustomErrorDecoder implements ErrorDecoder {


    @Override
    public Exception decode(String methodKey, Response response) {
        //START DECODING ORIGINAL ERROR MESSAGE
        String errorMessage = null;
        Reader reader = null;
        //capturing error message from response body.
        try {
            final var body = response.body();
            if (body != null) {
                reader = body.asReader(StandardCharsets.UTF_8);
                String result = IOUtils.toString(reader);
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                FeignException exceptionMessage = mapper.readValue(result,
                        FeignException.class);
                errorMessage = exceptionMessage.getMessage();
            } else {
                log.error("Null body");
            }
        } catch (IOException e) {
            log.error("IO Exception on reading exception message feign client" + e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("IO Exception on reading exception message feign client" + e);
            }
        }
        //END DECODING ORIGINAL ERROR MESSAGE

        switch (response.status()) {
        case 400:
            log.error("Error in request went through feign client httpStatus={} errorMessage={} ", response.status(), errorMessage);
            //handle exception
            return new Exception("Bad Request Through Feign");
        case 401:
            log.error("Error in request went through feign client httpStatus={} errorMessage={} ", response.status(), errorMessage);
            //handle exception
            return new Exception("Unauthorized Request Through Feign");
        case 404:
            log.error("Error in request went through feign client httpStatus={} errorMessage={} ", response.status(), errorMessage);
            //handle exception
            return new Exception("Unidentified Request Through Feign");
        default:
            log.error("Error in request went through feign client httpStatus={} errorMessage={} ", response.status(), errorMessage);
            //handle exception
            return new Exception("Common Feign Exception");
        }
    }
}