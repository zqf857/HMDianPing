package com.hmdp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class HmDianPingApplicationTests {

    @Test
    public void isEmpty(){
        String a = "";
        log.info(String.valueOf(a.isEmpty()));
    }
}
