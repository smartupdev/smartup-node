package global.smartup.node.match.common;

public enum OrderType {

    Buy("buy"), Sell("sell");

    private String value;

    private OrderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
