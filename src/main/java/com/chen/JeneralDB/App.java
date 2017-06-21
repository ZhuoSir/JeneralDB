package com.chen.JeneralDB;

/**
 *
 * Created by sunny on 17-6-21.
 */
public class App {

    // 创建数据库映射对象
    public static void main(String[] args) throws Exception {
        DBFactory.getInstance().createEntityFromDataBase();
    }
}
