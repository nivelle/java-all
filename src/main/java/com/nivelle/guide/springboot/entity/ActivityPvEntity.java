package com.nivelle.guide.springboot.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class ActivityPvEntity {


    private Integer id;

    private String activityId;

    private Integer positionType;

    private String deviceType;

    private String ip;

    private String deviceNo;

    private String createTime;

    private String updateTime;


}
