package bean;

import com.chen.JeneralDB.DataTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunny on 2016/12/10.
 */
public class TestApp {

    public static void main(String[] args) {
        Person person1 = new Person(1, "chen", 2);
        Person person2 = new Person(2, "wang", 2);
        Person person3 = new Person(3, "zhang", 2);
        Person person4 = new Person(4, "li", 2);
        List<Person> personArr = new ArrayList<Person>();
        personArr.add(person1);
        personArr.add(person2);
        personArr.add(person3);
        personArr.add(person4);
//        List<Person> persons = new ArrayList<Person>();
//        persons.add(person1);
//        persons.add(person2);
//        persons.add(person3);
        try {
            DataTable dataTable1 = new DataTable(personArr);
            DataTable dataTable2 = dataTable1.clone();
//            dataTable1.removeRowAtIndex(2);
            dataTable1.removeRowFromStartToEnd(1,3);
            System.out.println(dataTable1.equals(dataTable2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
