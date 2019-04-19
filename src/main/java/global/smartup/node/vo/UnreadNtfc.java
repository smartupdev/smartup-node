package global.smartup.node.vo;

import global.smartup.node.po.Notification;

import java.util.List;

public class UnreadNtfc {

    private Integer count;

    private List<Notification> list;


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Notification> getList() {
        return list;
    }

    public void setList(List<Notification> list) {
        this.list = list;
    }
}
