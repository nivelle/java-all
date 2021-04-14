package com.nivelle.spring.springboot.service;

import com.nivelle.spring.pojo.User;
import org.springframework.stereotype.Service;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/14
 */
@Service
public class UserService {

    public User getUserById(int id) {
        return new User(1, "jessy");
    }

    public User getUserByName(String userName) {
        return new User(1, "jessy");
    }
}
