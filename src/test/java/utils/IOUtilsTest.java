package utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

@Slf4j
public class IOUtilsTest {
    @Test
    public void readData() throws Exception {
        String data = "abcd123";
        StringReader sr = new StringReader(data);
        BufferedReader br = new BufferedReader(sr);

        log.debug("parse body : {}", IOUtils.readData(br, data.length()));
    }
}
