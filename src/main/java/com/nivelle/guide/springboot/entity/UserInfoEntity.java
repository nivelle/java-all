package com.nivelle.guide.springboot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class UserInfoEntity {

    private Long id;
    private String userName;
    private String name;
    private String password;
    private String salt;
    private String state;
}
