package com.nivelle.base.jdk.java8;

import org.checkerframework.checker.units.qual.C;

import java.text.DateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * java8的时间处理
 *
 * @author nivelle
 * @date 2020/04/02
 */
public class Java8DateTest {

    public static void main(String[] args) throws Exception {
        System.out.println("clock========================");
        // 时钟
        // Clock是关联上时区的时钟，Clock可以获取时间戳和时区ZoneId，用来代替System.currentTimeMillis()和TimeZone.getDefault()。时钟知晓时区，可以用来代替System.currentTimeMillis()来检索自Unix EPOCH以来的当前时间（以毫秒为单位）。在时间轴上的某一时刻用Instant表示。Instant可以创建遗留的java.util.Date 对象。
        Clock clock = Clock.systemDefaultZone();
        long millis = clock.millis();
        System.out.println("当前系统默认时刻：" + millis);
        System.out.println("系统默认时区：" + clock.getZone());
        Clock pastClock = Clock.offset(clock, Duration.ofMillis(-1000));
        System.out.println("1000毫秒之前的时刻：" + pastClock);
        System.out.println("时刻差值：" + (clock.millis() - pastClock.millis()));
        /**
         * 时间戳（瞬时时间，带时区）通过时刻直接获取时间戳
         */
        Instant instant = clock.instant();
        System.out.println("瞬时时间：" + instant);
        Date nowDate = Date.from(instant);
        System.out.println("瞬时时间转为日期时间：" + nowDate);
        //时区:时区是通过 ZoneId来表示，它提供了很多静态方法。时区定义了在瞬间和本地日期和时间之间转换的重要偏移。
        System.out.println(ZoneId.getAvailableZoneIds());
        System.out.print("给时钟设置时刻：" + clock.withZone(ZoneId.of("Asia/Shanghai")));
        //本地时间:LocalTime表示没有时区的时间，如晚上10点 或者 17:30:15。
        ZoneId zone1 = ZoneId.of("Europe/Berlin");
        ZoneId zone2 = ZoneId.of("Asia/Shanghai");
        LocalTime berlinNow = LocalTime.now(zone1);
        LocalTime shanghaiNow = LocalTime.now(zone2);
        System.out.println("时区差异导致的时间前后差异：" + berlinNow.isBefore(shanghaiNow));
        long minutesBetween = ChronoUnit.HOURS.between(berlinNow, shanghaiNow);
        System.out.println("柏林和上海时差：" + minutesBetween);
        //周期计时的TickDuration，截取时间到最接近的上个周期或下个周期的时间。注意：TickDuration不会把当前时间点作为周期的起始时间
        Clock clock1 = Clock.systemDefaultZone();
        Clock nearestHourClock = Clock.tick(clock1, Duration.ofMillis(100));
        System.out.println("tick tick =========");
        System.out.println("clock:" + clock1.instant());
        System.out.println("clock tick1:" + nearestHourClock.instant());
        Thread.sleep(10);
        System.out.println("after clock:" + clock1.instant());
        System.out.println("after clock tick1:" + nearestHourClock.instant());
        System.out.println("tick tick =========");

        Clock fixedClock = Clock.fixed(clock.instant(), ZoneId.systemDefault());
        System.out.println("fixed clock before:" + fixedClock.instant());
        Thread.sleep(10);
        System.out.println("fixed clock after:" + fixedClock.instant());

        System.out.println("根据毫秒生成instant:" + Instant.ofEpochMilli(new Date().getTime()));
        Instant preInstant = Instant.ofEpochSecond(1609741558, 1);
        System.out.println("过去时间和当前时间的相差值,可以指定单位TemporalUnit:" + preInstant.until(Instant.now(), ChronoUnit.MINUTES));
        Instant fixInstant = instant.minus(1, ChronoUnit.MINUTES).plus(2, ChronoUnit.DAYS);
        System.out.println("分钟数减1天数加2：" + fixInstant);

        System.out.println("temporal =======================");
        //时间类的统一接口。定义通用的方法操作


        System.out.println("localTime=========================");
        //LocalTime是用来操作时分秒的类，外加精确到纳秒级别；无时区概念，转Instant需要先设置时区
        LocalTime localTime = LocalTime.of(23, 59, 59);
        System.out.println(localTime);
        //格式化日期时间
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("HHmm");
        LocalTime localTime2 = LocalTime.parse("1237", germanFormatter);
        System.out.println("字符串转时间：" + localTime2);
        System.out.println("距离凌晨的秒数：" + LocalTime.ofSecondOfDay(123L));
        System.out.println("距离凌晨的纳秒数：" + LocalTime.ofNanoOfDay(123L));

        System.out.println("localDateTime 转 localDate:" + localTime.atDate(LocalDate.now()));


        //日期（2018-09-24，不带时区）是用来操作年月日的类：表示的时间单位是截止到日，不包括小时及后面的单位
        System.out.println("localDate====================");
        LocalDate localDate = LocalDate.now();
        System.out.println("localDate now:" + localDate);
        System.out.println("localDate of date:" + LocalDate.of(2020, 1, 11));
        System.out.println("当天是当月的第几天：" + localDate.getDayOfMonth());
        System.out.println("当天是当年的第几天：" + localDate.getDayOfYear());
        System.out.println("当天是当周的第几天：" + localDate.getDayOfWeek());
        System.out.println("是否是闰年：" + localDate.isLeapYear());
        System.out.println("为日期设置时刻：" + localDate.atTime(localTime));
        System.out.println("设置时间为今天的凌晨：" + localDate.atStartOfDay());
        localDate = LocalDate.parse("20210111", DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println("格式化日期时间：" + localDate);
        //本地日期
        LocalTime time = LocalTime.of(23, 59, 59);
        System.out.println(time);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        LocalDate yesterday = tomorrow.minusDays(2);
        System.out.println(today);
        System.out.println(tomorrow);
        System.out.println(yesterday);

        System.out.println("DateTImeFormatter====================");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeFormatter = dateTimeFormatter.withZone(ZoneId.of("America/El_Salvador"));
        System.out.println(dateTimeFormatter.parse("2021-03-19 12:12:12"));
        System.out.println(dateTimeFormatter.parse("2021-03-19 12:12:12").getLong(ChronoField.INSTANT_SECONDS));

        System.out.println();
    }
}
