package global.smartup.node.po;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "market")
public class Market {

    public interface Add {}

    public interface CheckName {}

    @Id
    private String marketId;

    @Column(name="tx_hash")
    private String txHash;

    // @NotNull(message = "{market_creator_address_format_error}", groups = Market.Add.class)
    // @NotEmpty(message = "{market_creator_address_format_error}", groups = Market.Add.class)
    // @Size(max = 42, min = 42, message = "{market_creator_address_format_error}", groups = Market.Add.class)
    @Column(name="creator_address")
    private String creatorAddress;

    @Column(name="market_address")
    private String marketAddress;

    @NotNull(message = "{market_name_empty_error}", groups = {Market.Add.class, Market.CheckName.class})
    @NotEmpty(message = "{market_name_empty_error}", groups = {Market.Add.class, Market.CheckName.class})
    @Size(max = 30, min = 2, message = "{market_name_length_error}", groups = {Market.Add.class, Market.CheckName.class})
    @Column(name="name")
    private String name;

    @Size(max = 300, min = 5, message = "{market_description_length_error}", groups = Market.Add.class)
    @Column(name="description")
    private String description;

    @Column(name="type")
    private String type;

    /**
     * {@link global.smartup.node.constant.PoConstant.Market.Stage}
     */
    @Column(name="stage")
    private String stage;

    @Column(name="create_time")
    private Date createTime;


    @Transient
    private MarketData data;

    @Transient
    private Boolean isCollect;



    public Boolean getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(Boolean collect) {
        isCollect = collect;
    }

    public MarketData getData() {
        return data;
    }

    public void setData(MarketData data) {
        this.data = data;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

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
