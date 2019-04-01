package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "")
    private String name;

    @Column(name = "avatar_ipfs_hash")
    private String avatarIpfsHash;

    @Column(name = "create_time")
    private Date createTime;

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarIpfsHash() {
        return avatarIpfsHash;
    }

    public void setAvatarIpfsHash(String avatarIpfsHash) {
        this.avatarIpfsHash = avatarIpfsHash;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
