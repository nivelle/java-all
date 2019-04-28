package com.nivelle.guide.designpatterns.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyUser {

    private final String CompanyName;

    private final String jobName;

    private final String workDesc;

    private final int salary;

    private final int age;

    private final boolean isMarry;

    public CompanyUser(Builder builder) {
        this.CompanyName = builder.CompanyName;
        this.jobName = builder.jobName;
        this.workDesc = builder.workDesc;
        this.salary = builder.salary;
        this.age = builder.age;
        this.isMarry = builder.isMarry;
    }


    public static class Builder

    {
        public final String CompanyName;

        public String jobName;

        private String workDesc;

        private int salary;

        private int age;

        private boolean isMarry;

        public Builder(String companyName) {
            this.CompanyName = companyName;
        }

        public Builder withJobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public Builder withWorkDesc(String workDesc) {
            this.workDesc = workDesc;
            return this;
        }

        public Builder withSalary(int salary) {
            this.salary = salary;
            return this;
        }

        public Builder withAge(int age) {
            this.age = age;
            return this;
        }

        public Builder withIsMarry(boolean isMarry) {
            this.isMarry = isMarry;
            return this;
        }

        public CompanyUser build() {
            return new CompanyUser(this);
        }


    }
}
