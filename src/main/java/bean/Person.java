package bean;

/**
 * Created by sunny-chen on 16/11/27.
 */
public class Person {

    private Integer idCard;
    private String name;
    private Integer sex;

    public Integer getIdCard() {
        return idCard;
    }

    public void setIdCard(Integer idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "name : " + name + " sex:" + sex + " idcard:" + idCard;
    }
}
