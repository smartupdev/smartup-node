package global.smartup.node.po;

import global.smartup.node.util.Pagination;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "reply")
public class Reply {

    public interface Add {}

    @Id
    @Column(name="reply_id")
    private Long replyId;

    @Column(name="post_id")
    private Long postId;

    @Column(name="father_id")
    private Long fatherId;

    @Column(name="user_address")
    private String userAddress;

    @NotNull(message = "{reply_content_empty_error}", groups = Reply.Add.class)
    @NotEmpty(message = "{reply_content_empty_error}", groups = Reply.Add.class)
    @Size(max = 300, min = 2, message = "{reply_content_length_error}", groups = Reply.Add.class)
    @Column(name="content")
    private String content;

    @Column(name="create_time")
    private Date createTime;

    private Pagination<Reply> childrenPage;

    public Long getReplyId() {
        return replyId;
    }

    public void setReplyId(Long replyId) {
        this.replyId = replyId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Pagination<Reply> getChildrenPage() {
        return childrenPage;
    }

    public void setChildrenPage(Pagination<Reply> childrenPage) {
        this.childrenPage = childrenPage;
    }
}
