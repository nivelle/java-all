package com.nivelle.spring.springmvc;
import com.nivelle.spring.pojo.Person;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
/**
 * 自定义验证器
 *
 * @author fuxinzhong
 * @date 2021/04/16
 */
public class PersonValidator implements Validator{

    /**
     * This Validator validates *just* Person instances
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "name", "name.empty");
        Person p = (Person) obj;
        if (p.getAge() < 0) {
            e.rejectValue("age", "negative value","年龄不能为负值");
            System.out.println("年龄不能为负数");
        } else if (p.getAge() > 110) {
            e.rejectValue("age", "too old","年龄不能超过110");
            System.out.println("年龄不能超过110");
        }
    }

}
