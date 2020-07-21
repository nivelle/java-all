package com.nivelle.middleware.pojo;

import lombok.*;

import java.util.Date;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2019/07/25
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {

    private String id;

    private String msg;

    private Date sendTime;
}
