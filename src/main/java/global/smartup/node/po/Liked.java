package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "liked")
public class Liked {

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "market_address")
    private String marketAddress;

    @Column(name = "type")
    private String type;

    @Column(name = "object_mark")
    private String objectMark;

    @Column(name = "create_time")
    private Date createTime;



    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectMark() {
        return objectMark;
    }

    public void setObjectMark(String objectMark) {
        this.objectMark = objectMark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
