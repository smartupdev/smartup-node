package global.smartup.node.po;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "user")
public class User {

    public interface Add {}

    @Id
    @Column(name = "user_address")
    @NotEmpty(message = "{address_format_error}", groups = User.Add.class)
    @NotNull(message = "{address_format_error}", groups = User.Add.class)
    @Size(max = 42, min = 42, message = "{address_format_error}", groups = User.Add.class)
    private String userAddress;

    @Column(name = "name")
    @Size(max = 32, message = "{user_name_max_size_error}", groups = User.Add.class)
    private String name;

    @Column(name = "avatar_ipfs_hash")
    @Size(max = 64, message = "{user_avatar_ipfs_hash_max_size_error}", groups = User.Add.class)
    private String avatarIpfsHash;

    @Column(name = "code")
    private String code;

    @Column(name = "create_time")
    private Date createTime;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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
