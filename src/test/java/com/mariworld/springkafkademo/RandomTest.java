package com.mariworld.springkafkademo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

@Slf4j
public class RandomTest {

    @Test
    public void randomIdTest(){
        Instant instant = Instant.now();
        log.info("instant : {} ", instant);
        long seconds = instant.getEpochSecond();
        long nano = instant.getNano();
        log.warn("seconds : {} ", seconds);
        log.warn("nano : {} ", nano);
        log.info("---------------------------");
        Instant instant2 = Instant.now();
        long seconds2 = instant2.getEpochSecond();
        long nano2 = instant2.getNano();
        log.warn("seconds : {} ", seconds2);
        log.warn("nano : {} ", nano2);
        //2022-07-08T06:30:42.396295Z 396295000
        //220708063042396295 -> 18자리 (마이크로세컨드까지)

        UUID uuid = UUID.randomUUID();
        log.info(uuid.toString());
        int length = uuid.toString().length();
        String substring = uuid.toString().substring(length - 12, length);
        log.info(substring);
    }
}
