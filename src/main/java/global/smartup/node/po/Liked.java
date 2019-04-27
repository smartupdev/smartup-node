package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "liked")
public class Liked {

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "market_Id")
    private String marketId;

    @Column(name = "type")
    private String type;

    @Column(name = "object_mark")
    private String objectMark;

    @Column(name = "is_like")
    private Boolean isLike;

    @Column(name = "create_time")
    private Date createTime;



    public Boolean getIsLike() {
        return isLike;
    }

    public void setIsLike(Boolean like) {
        isLike = like;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
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
