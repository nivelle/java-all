package com.nivelle.spring.springboot.params;


import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ActivityParams {


    @NotBlank(message = "activityId is null")
    private String activityId;

    @NotNull(message = "positionType is null")
    private Integer positionType;//1.活动页 2.掌阅精选 3.发布内容

    @NotBlank(message = "ip is null")
    private String ip;//ip

    @NotBlank(message = "deviceType is null")
    private String deviceType;//设备类型


    private String deviceNo;//设备号


}
