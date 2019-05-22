package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Table(name = "notification")
public class Notification {

    @Id
    @Column(name="notification_id")
    private Long notificationId;

    @Column(name="user_address")
    private String userAddress;

    @Column(name="style")
    private String style;

    @Column(name="type")
    private String type;

    @Column(name="content")
    private String content;

    @Column(name="is_read")
    private Boolean isRead;

    @Column(name="create_time")
    private Date createTime;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "title_zh_cn")
    private String titleZhCn;

    @Column(name = "title_zh_tw")
    private String titleZhTw;

    @Column(name = "text_en")
    private String textEn;

    @Column(name = "text_zh_cn")
    private String textZhCN;

    @Column(name = "text_zh_tw")
    private String textZhTW;



    @Transient
    private String title;

    @Transient
    private String text;


    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleZhCn() {
        return titleZhCn;
    }

    public void setTitleZhCn(String titleZhCn) {
        this.titleZhCn = titleZhCn;
    }

    public String getTitleZhTw() {
        return titleZhTw;
    }

    public void setTitleZhTw(String titleZhTw) {
        this.titleZhTw = titleZhTw;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    public String getTextZhCN() {
        return textZhCN;
    }

    public void setTextZhCN(String textZhCN) {
        this.textZhCN = textZhCN;
    }

    public String getTextZhTW() {
        return textZhTW;
    }

    public void setTextZhTW(String textZhTW) {
        this.textZhTW = textZhTW;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
