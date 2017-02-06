package test;

import bean.MemMember;
import com.chen.JeneralDB.DBFactory;
import com.chen.JeneralDB.DBUtil;
import com.chen.JeneralDB.DataTable;
import com.chen.JeneralDB.filter.DataTableFilter;
import com.chen.JeneralDB.jdbc.Connections;
import com.chen.JeneralDB.transaction.JdbcTransaction;
import com.chen.JeneralDB.transaction.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sunny on 2017/1/2.
 */
public class App {

    public static void main(String[] args) throws IllegalAccessException {

//        dataTable();
        dbFactory();
//        dataTableFilter();
//        save();
//        insertSql();
//        save2();
    }

    public static void dataTableFilter() {
        DBUtil dbUtil = DBUtil.getInstance();
        String sql = "select * from yyg_goods";

        try {
            DataTable dataTable = dbUtil.queryDataTable(sql);
            String columnName = "AttributeArea";
            int num = 2;

            dataTable = dataTable.filter(new DataTableFilter() {

                @Override
                public boolean accept(List<String> column, Object[] row) {
                    int index = column.indexOf(columnName);
                    return row[index].equals(num);
                }

            });

            dataTable.print();

//            List<Admin> personList = newDataTable.toBeanList(Admin.class);
//            personList.forEach(person -> System.out.println(person.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void dataTable() {
        DBUtil dbUtil = DBUtil.getInstance();
//        String sql = "SELECT a.nick as n, a.name as na, p.pname as proname, p.createTime FROM admin a LEFT JOIN ppmproject p ON a.id = p.ownerid";
        String sql = "select * from admin";

        try {
            DataTable dataTable = dbUtil.queryDataTable(sql);
            System.out.println(dataTable.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dbFactory() {
        try {
            List<String> tableNames = new ArrayList<String>() {
                {
                    add("mall_goods");
                    add("mem_address");
                    add("web_user");
                }
            };
            DBFactory.getInstance().createEntityFromDataBase();
//            DBFactory.getInstance().createEntitysByTableNames(tableNames, "D:\\JAVA\\JeneralDB\\src\\main\\java\\bean2", "bean2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void query() {
//        String sql = "SELECT mm.*, ma.DetailAddress FROM mem_member mm LEFT JOIN mem_address ma ON mm.MemberID = ma.MemberID WHERE ma.MemberID = 100157";
        String sql = "SELECT * FROM mem_member";
        DBUtil dbUtil = DBUtil.getInstance();

        try {
            DataTable dataTable = dbUtil.queryDataTable(sql);
            dataTable.print();
            List<MemMember> memMembers = dataTable.toBeanList(MemMember.class);
            memMembers.forEach(memMember -> System.out.println(memMember.toString()));

            dataTable = dataTable.filter(new DataTableFilter() {
                @Override
                public boolean accept(List<String> column, Object[] row) {
                    int index = column.indexOf("Status");
                    return row[index].equals(1);
                }
            });

            dataTable.removeRowAtIndex(0);
            dataTable.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void save() {
        MemMember memMember = new MemMember();
        memMember.setAddTime(new Date());
        memMember.setNickName("小凤凰");
        memMember.setPassword("123456789");
        memMember.setPhoneNumber("18516962543");
        memMember.setStatus(0);
        memMember.setRealName("陈卓");
        memMember.setHeadPicFileURL("sdsdsdsdsd");
        memMember.setWebLoginFlag(true);
        memMember.setWXLoginFlag(true);
        DBUtil dbUtil = DBUtil.getInstance();
        try {
            dbUtil.save(memMember, "mem_member");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void save2() {
        MemMember memMember = new MemMember();
        memMember.setAddTime(new Date());
        memMember.setNickName("chenzhuo");
        memMember.setPassword("123456789");
        memMember.setPhoneNumber("18516962543");
        memMember.setStatus(0);
        memMember.setRealName("hi");
        memMember.setHeadPicFileURL("sdsdsdsdsd");
        memMember.setWebLoginFlag(true);
        memMember.setWXLoginFlag(true);

        DBUtil dbUtil = DBUtil.getInstance();
        try {
            Transaction transaction = new JdbcTransaction();
            dbUtil.save(Connections.getConnection(), memMember, "mem_member");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertSql() throws IllegalAccessException {
        MemMember memMember = new MemMember();
        memMember.setAddTime(new Date());
        memMember.setNickName("小凤凰");
        memMember.setPassword("123456789");
        memMember.setPhoneNumber("18516962543");
        memMember.setStatus(0);
        memMember.setRealName("陈卓");
        memMember.setHeadPicFileURL("sdsdsdsdsd");
        memMember.setWebLoginFlag(true);
        memMember.setWXLoginFlag(true);

        System.out.println(DBUtil.getInstance().buildInsertSql(memMember, "mem_member"));
    }
}
