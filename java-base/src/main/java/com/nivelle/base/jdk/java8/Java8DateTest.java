package com.nivelle.base.jdk.java8;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * java8的时间处理
 *
 * @author nivell
 * @date 2020/04/02
 */
public class Java8DateTest {

    public static void main(String[] args) {
        // 时钟
        // 时钟提供了对当前日期和时间的访问。时钟知晓时区，可以用来代替System.currentTimeMillis()来检索自Unix EPOCH以来的当前时间（以毫秒为单位）。在时间轴上的某一时刻用Instant表示。Instant可以创建遗留的java.util.Date 对象。
        Clock clock = Clock.systemDefaultZone();
        long millis = clock.millis();
        System.out.println(millis);

        Instant instant = clock.instant();
        Date nowDate = Date.from(instant);
        System.out.println(nowDate);


        //时区:时区是通过 ZoneId来表示，它提供了很多静态方法。时区定义了在瞬间和本地日期和时间之间转换的重要偏移。
        //System.out.println(ZoneId.getAvailableZoneIds());

        System.out.print(ZoneId.of("Asia/Shanghai"));


        //本地时间:LocalTime表示没有时区的时间，如晚上10点 或者 17:30:15。
        ZoneId zone1 = ZoneId.of("Europe/Berlin");
        ZoneId zone2 = ZoneId.of("Asia/Shanghai");

        LocalTime now1 = LocalTime.now(zone1);
        LocalTime now2 = LocalTime.now(zone2);

        System.out.println(now1.isBefore(now2));
        long minutesBetween = ChronoUnit.HOURS.between(now1, now2);
        System.out.println(minutesBetween);


        LocalTime late = LocalTime.of(23, 59, 59);
        System.out.println(late);

        //格式化日期时间
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("HHmm");

        LocalTime leetTime = LocalTime.parse("1237", germanFormatter);
        System.out.println(leetTime);

        //本地日期

        LocalTime time = LocalTime.of(23, 59, 59);
        System.out.println(time);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        LocalDate yesterday = tomorrow.minusDays(2);
        System.out.println(today);
        System.out.println(tomorrow);
        System.out.println(yesterday);
    }
}
