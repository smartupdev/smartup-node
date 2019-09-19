package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Table(name = "market")
public class Market {

    @Id
    private String marketId;

    @Column(name="creator_address")
    private String creatorAddress;

    @Column(name="market_address")
    private String marketAddress;

    @Column(name="name")
    private String name;

    @Column(name="symbol")
    private String symbol;

    @Column(name="cover")
    private String cover;

    @Column(name="photo")
    private String photo;

    @Column(name="description")
    private String description;

    @Column(name="detail")
    private String detail;

    @Column(name="tx_hash")
    private String txHash;

    /**
     * {@link global.smartup.node.constant.PoConstant.Market.Status}
     */
    @Column(name="status")
    private String status;

    /**
     * {@link global.smartup.node.constant.PoConstant.Market.Stage}
     */
    @Column(name="stage")
    private String stage;

    @Column(name="init_sut")
    private BigDecimal initSut;

    @Column(name = "ct_count")
    private BigDecimal ctCount;

    @Column(name = "ct_price")
    private BigDecimal ctPrice;

    @Column(name = "ct_recycle_price")
    private BigDecimal ctRecyclePrice;

    // 市场中剩余的CT
    @Column(name = "ct_rest")
    private BigDecimal ctRest;

    @Column(name = "closing_time")
    private Date closingTime;

    @Column(name="create_time")
    private Date createTime;



    @Transient
    private MarketData data;

    @Transient
    private Boolean isCollected;

    @Transient
    private List<BigDecimal> sevenDayNode;

    @Transient
    private User creator;




    public String getStage() {
        return stage;
    }

    public Market setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public BigDecimal getCtRest() {
        return ctRest;
    }

    public Market setCtRest(BigDecimal ctRest) {
        this.ctRest = ctRest;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public Market setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public Date getClosingTime() {
        return closingTime;
    }

    public Market setClosingTime(Date closingTime) {
        this.closingTime = closingTime;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Market setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public BigDecimal getInitSut() {
        return initSut;
    }

    public void setInitSut(BigDecimal initSut) {
        this.initSut = initSut;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BigDecimal> getSevenDayNode() {
        return sevenDayNode;
    }

    public void setSevenDayNode(List<BigDecimal> sevenDayNode) {
        this.sevenDayNode = sevenDayNode;
    }

    public Boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(Boolean collect) {
        isCollected = collect;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getCtCount() {
        return ctCount;
    }

    public void setCtCount(BigDecimal ctCount) {
        this.ctCount = ctCount;
    }

    public BigDecimal getCtPrice() {
        return ctPrice;
    }

    public void setCtPrice(BigDecimal ctPrice) {
        this.ctPrice = ctPrice;
    }

    public BigDecimal getCtRecyclePrice() {
        return ctRecyclePrice;
    }

    public void setCtRecyclePrice(BigDecimal ctRecyclePrice) {
        this.ctRecyclePrice = ctRecyclePrice;
    }
}
