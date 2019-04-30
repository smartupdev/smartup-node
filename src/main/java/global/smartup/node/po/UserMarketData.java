package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "user_market_data")
public class UserMarketData {

    @Id
    @Column(name = "user_address")
    private String userAddress;

    @Id
    @Column(name = "market_id")
    private String marketId;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "reply_count")
    private Integer replyCount;

    @Column(name = "received_like_count")
    private Integer receivedLikeCount;



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

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getReceivedLikeCount() {
        return receivedLikeCount;
    }

    public void setReceivedLikeCount(Integer receivedLikeCount) {
        this.receivedLikeCount = receivedLikeCount;
    }

}
