package global.smartup.node.po;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "post")
public class Post {

    public interface Add {}

    @Id
    @Column(name="post_id")
    private Long postId;

    @Column(name="type")
    private String type;

    @Column(name="photo")
    private String photo;

    @Column(name="market_address")
    private String marketAddress;

    @Column(name = "market_id")
    private String marketId;

    @NotNull(message = "{post_user_address_format_error}", groups = Post.Add.class)
    @NotEmpty(message = "{post_user_address_format_error}", groups = Post.Add.class)
    @Size(max = 42, min = 42, message = "{post_user_address_format_error}", groups = Post.Add.class)
    @Column(name="user_address")
    private String userAddress;

    @NotNull(message = "{post_title_empty_error}", groups = Post.Add.class)
    @NotEmpty(message = "{post_title_empty_error}", groups = Post.Add.class)
    @Size(max = 30, min = 2, message = "{post_title_length_error}", groups = Post.Add.class)
    @Column(name="title")
    private String title;

    @NotNull(message = "{post_description_empty_error}", groups = Post.Add.class)
    @NotEmpty(message = "{post_description_empty_error}", groups = Post.Add.class)
    @Size(max = 300, min = 2, message = "{post_description_length_error}", groups = Post.Add.class)
    @Column(name="description")
    private String description;

    @Column(name="create_time")
    private Date createTime;


    @Transient
    private User user;

    @Transient
    private PostData data;

    @Transient
    private Reply lastReply;

    @Transient
    private Boolean isLiked;

    @Transient
    private Boolean isDisliked;

    @Transient
    private Boolean isCollected;



    public Boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(Boolean collected) {
        isCollected = collected;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Reply getLastReply() {
        return lastReply;
    }

    public void setLastReply(Reply lastReply) {
        this.lastReply = lastReply;
    }

    public PostData getData() {
        return data;
    }

    public void setData(PostData data) {
        this.data = data;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean liked) {
        isLiked = liked;
    }

    public Boolean getIsDisliked() {
        return isDisliked;
    }

    public void setIsDisliked(Boolean disliked) {
        isDisliked = disliked;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
