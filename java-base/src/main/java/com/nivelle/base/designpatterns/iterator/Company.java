package com.nivelle.base.designpatterns.iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Company implements Organize {


    private List<User> users;


    private int numbers;


    public boolean addNewUser(User user) {
        if (Objects.isNull(users)) {
            users = new ArrayList<User>();
        }
        numbers++;
        return users.add(user);
    }

    public User getUser(int index) {

        return users.get(index);
    }


    public int getNumbers() {
        return numbers;
    }

    public Iterator iterator() {
        return new CompanyIterator(this);
    }
}
