package ticketing;

public class Promo {
    private final PromoType type;  // "NONE", "PERCENT", "FIXED"
    private final double value; // percent or fixed amount

    public Promo(PromoType type, double value) {
        this.type = type;
        this.value = value;
    }

    public PromoType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }
}
