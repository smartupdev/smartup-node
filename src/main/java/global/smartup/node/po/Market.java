package global.smartup.node.po;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "market")
public class Market {

    public interface Add {}

    @Id
    @Column(name="tx_hash")
    @NotNull(message = "{market_tx_hash_empty_error}", groups = Market.Add.class)
    @NotEmpty(message = "{market_tx_hash_empty_error}", groups = Market.Add.class)
    @Size(max = 66, min = 66, message = "{market_tx_hash_format_error}", groups = Market.Add.class)
    private String txHash;

    @NotNull(message = "{market_creator_address_format_error}", groups = Market.Add.class)
    @NotEmpty(message = "{market_creator_address_format_error}", groups = Market.Add.class)
    @Size(max = 42, min = 42, message = "{market_creator_address_format_error}", groups = Market.Add.class)
    @Column(name="creator_address")
    private String creatorAddress;

    @Column(name="market_address")
    private String marketAddress;

    @NotNull(message = "{market_name_empty_error}", groups = Market.Add.class)
    @NotEmpty(message = "{market_name_empty_error}", groups = Market.Add.class)
    @Size(max = 30, min = 2, message = "{market_name_length_error}", groups = Market.Add.class)
    @Column(name="name")
    private String name;

    @Size(max = 300, min = 5, message = "{market_description_length_error}", groups = Market.Add.class)
    @Column(name="description")
    private String description;

    @Column(name="type")
    private String type;

    // 状态
    // 创建中，已建成
    @Column(name="stage")
    private String stage;

    @Column(name="create_time")
    private Date createTime;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getCreatorAddress() {
        return creatorAddress;
    }

    public void setCreatorAddress(String creatorAddress) {
        this.creatorAddress = creatorAddress;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
