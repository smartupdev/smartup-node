package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "user_account")
public class UserAccount {

    @Id
    @Column(name="user_address")
    private String userAddress;

    @Column(name="sut")
    private BigDecimal sut;

    @Column(name="sut_amount")
    private BigDecimal sutAmount;

    @Column(name="update_time")
    private Date updateTime;



    @Transient
    private User user;



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public BigDecimal getSut() {
        return sut;
    }

    public void setSut(BigDecimal sut) {
        this.sut = sut;
    }

    public BigDecimal getSutAmount() {
        return sutAmount;
    }

    public void setSutAmount(BigDecimal sutAmount) {
        this.sutAmount = sutAmount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
