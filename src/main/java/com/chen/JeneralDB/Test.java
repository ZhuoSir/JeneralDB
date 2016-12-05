package com.chen.JeneralDB;

import bean.Person;

import java.util.List;

/**
 * Created by sunny-chen on 16/11/27.
 */
public class Test {

    public static void main(String[] args) {
        Person person = new Person();
        person.setIdCard(66666);
        person.setSex(2);
        person.setName("chenguo");
        try {
            DBUtil.save(person);
            String sql = "select * from person";
            List<Person> persons = DBUtil.queryBeanList(sql,Person.class);
            persons.forEach(System.out::println);
//            List<Map<String, Object>> maps = DBUtil.queryMapList(sql);
//            maps.forEach(System.out::println);
//            DBUtil.execute("delete from person");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
