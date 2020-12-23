package com.nivelle.rpc.dubbo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nivelle.rpc.dubbo.config.LocalDateTimeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * LocalDateTime在springBoot中的应用
 *
 * @author nivelle
 * @date 2019/12/26
 */
@NoArgsConstructor
@ToString
@Setter
@Getter
public class DateTimeInfo {

    /**
     * 将LocalDateTime字段以时间戳的方式返回给前端
     */
    @JsonSerialize(using = LocalDateTimeConverter.class)
    private LocalDateTime dateTime1;
    /**
     * 将LocalDateTime字段以指定格式化日期的方式返回给前端
     */
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime2;

    /**
     * 前端传入的日期进行格式
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime3;
}
