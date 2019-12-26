package com.nivelle.base.designpatterns.iterator;

public class CompanyIterator implements Iterator {

    private Company company;

    private int index;

    public CompanyIterator(Company company) {
        this.company = company;
        this.index = 0;
    }

    @Override
    public boolean hashNext() {
        if (index < company.getNumbers()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object next() {

        User user = company.getUser(index);
        index++;
        return user;

    }
}
