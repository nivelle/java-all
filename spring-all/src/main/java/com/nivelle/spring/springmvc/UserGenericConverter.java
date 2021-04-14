package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;
import com.nivelle.spring.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/14
 */
public class UserGenericConverter implements GenericConverter {

    @Autowired
    UserService userService;

    @Override
    public Object convert(Object source, TypeDescriptor sourceType,
                          TypeDescriptor targetType) {
        if (source == null || sourceType == null || targetType == null) {
            return null;
        }
        User user = null;
        if (sourceType.getType() == Integer.class) {
            user = userService.getUserById((Integer) source);
        } else if (sourceType.getType() == String.class) {
            user = userService.getUserByName((String) source);
        }
        return user;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<ConvertiblePair>();
        pairs.add(new ConvertiblePair(Integer.class, User.class));
        pairs.add(new ConvertiblePair(String.class, User.class));
        return pairs;
    }

}
