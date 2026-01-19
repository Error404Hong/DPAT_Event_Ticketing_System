package pattern_based_solution;

import ticketing.*;

import java.util.List;

// Strategy Design Pattern
interface SeatPricing {
    double getBasePrice();
}

class VipSeatPricing implements SeatPricing {
    public double getBasePrice() {
        return 180.0;
    }
}

class PremiumSeatPricing implements SeatPricing {
    public double getBasePrice() { return 120.0; }
}

class StandardSeatPricing implements SeatPricing {
    public double getBasePrice() {
        return 80.0;
    }
}

class BalconySeatPricing implements SeatPricing {
    public double getBasePrice() {
        return 60.0;
    }
}

class SeatPricingStrategyFactory {
    public static SeatPricing create(SeatZone zone) {
        return switch (zone) {
            case VIP -> new VipSeatPricing();
            case PREMIUM -> new PremiumSeatPricing();
            case STANDARD -> new StandardSeatPricing();
            case BALCONY -> new BalconySeatPricing();
        };
    }
}

// Decorator Design Pattern
interface PriceComponent {
    double getPrice();
}

class BaseSeatSubtotal implements PriceComponent {
    private final double seatSubtotal;

    public BaseSeatSubtotal(double seatSubtotal) {
        this.seatSubtotal = seatSubtotal;
    }

    public double getPrice() {
        return seatSubtotal;
    }
}

abstract class PriceDecorator implements PriceComponent {
    protected final PriceComponent inner;

    protected PriceDecorator(PriceComponent inner) {
        this.inner = inner;
    }
}

// Add weekend surcharge (+10%) to seat subtotal
class WeekendSurchargeDecorator extends PriceDecorator {
    public WeekendSurchargeDecorator(PriceComponent inner) {
        super(inner);
    }

    @Override
    public double getPrice() {
        return inner.getPrice() * 1.10;
    }
}

// Add a booking fee once per booking
class BookingFeeDecorator extends PriceDecorator {
    private final double fee;

    public BookingFeeDecorator(PriceComponent inner, double fee) {
        super(inner);
        this.fee = fee;
    }

    @Override
    public double getPrice() {
        return inner.getPrice() + fee;
    }
}

// Apply membership discount
class MembershipDiscountDecorator extends PriceDecorator {
    private final MembershipTier tier;

    public MembershipDiscountDecorator(PriceComponent inner, MembershipTier tier) {
        super(inner);
        this.tier = tier;
    }

    @Override
    public double getPrice() {
        double p = inner.getPrice();
        return switch (tier) {
            case GOLD -> p * 0.90;   // -10%
            case SILVER -> p * 0.95; // -5%
            case NONE -> p;
        };
    }
}

// Apply promo discount
class PromoDiscountDecorator extends PriceDecorator {
    private final Promo promo;

    public PromoDiscountDecorator(PriceComponent inner, Promo promo) {
        super(inner);
        this.promo = promo;
    }

    @Override
    public double getPrice() {
        double p = inner.getPrice();

        if (promo == null || promo.getType() == PromoType.NONE) {
            return p;
        }

        return switch (promo.getType()) {
            case PERCENT -> p * (1.0 - promo.getValue() / 100.0);
            case FIXED -> p - promo.getValue();
            case STUDENT -> p * (1.0 - promo.getValue() / 100.0);
            case NONE -> p;
        };
    }
}

// Dynamic Pricing for High Demand Event
class HighDemandDecorator extends PriceDecorator {
    public HighDemandDecorator(PriceComponent inner) {
        super(inner);
    }

    @Override
    public double getPrice() {
        return inner.getPrice() * 1.20;
    }
}

class PricingEngine {

    public double calculateFinalPrice(Event event, List<SeatZone> seats, MembershipTier tier, Promo promo) {

        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Seat selection cannot be empty");
        }

        // Strategy usage
        double seatSubtotal = 0.0;
        for (SeatZone zone : seats) {
            SeatPricing strategy = SeatPricingStrategyFactory.create(zone);
            seatSubtotal += strategy.getBasePrice();
        }

        // Decorator stacking
        PriceComponent price = new BaseSeatSubtotal(seatSubtotal);

        if (event.isWeekend()) {
            price = new WeekendSurchargeDecorator(price);
        }

        price = new BookingFeeDecorator(price, event.getBookingFee());
        price = new MembershipDiscountDecorator(price, tier);

        if (promo != null && promo.getType() != PromoType.NONE) {
            price = new PromoDiscountDecorator(price, promo);
        }

        if(event.isHighDemand()) {
            price = new HighDemandDecorator(price);
        }

        double finalTotal = price.getPrice();
        if (finalTotal < 0) finalTotal = 0;

        return round2(finalTotal);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}


public class PatternBasedEventTicketing {
    public static void main(String[] args) {
        PricingEngine engine = new PricingEngine();

        // 1) Concert – Weekend + GOLD + 10% Promo
        Event concert = new Event("Campus Concert", true, 5.00, true);
        List<SeatZone> concertSeats = List.of(SeatZone.VIP, SeatZone.VIP, SeatZone.PREMIUM);
        Promo concertPromo = new Promo(PromoType.STUDENT, 15.0);

        double concertTotal = engine.calculateFinalPrice(concert, concertSeats, MembershipTier.GOLD, concertPromo);

        System.out.println("=== Payment Summary 1 (Concert) ===");
        System.out.println("Event: " + concert.getName());
        System.out.println("Seats: " + concertSeats);
        System.out.println("Membership: GOLD");
        System.out.println("Promo: PERCENT 10%");
        System.out.println("Final Total: RM " + concertTotal);
        System.out.println();

        // 2) Movie Night – Weekday + NO discount
        Event movieNight = new Event("Movie Night", false, 2.00, false);
        List<SeatZone> movieSeats = List.of(SeatZone.STANDARD, SeatZone.STANDARD, SeatZone.STANDARD);

        double movieTotal = engine.calculateFinalPrice(movieNight, movieSeats, MembershipTier.NONE, null);

        System.out.println("=== Payment Summary 2 (Movie Night) ===");
        System.out.println("Event: " + movieNight.getName());
        System.out.println("Seats: " + movieSeats);
        System.out.println("Membership: NONE");
        System.out.println("Promo: NONE");
        System.out.println("Final Total: RM " + movieTotal);
        System.out.println();


        // 3) Sport Day – Weekend + SILVER + RM15 FIXED Promo
        Event sportDay = new Event("Campus Sport Day", true, 5.00, false);
        List<SeatZone> sportSeats = List.of(SeatZone.BALCONY, SeatZone.BALCONY, SeatZone.BALCONY);
        Promo sportPromo = new Promo(PromoType.FIXED, 15.0);

        double sportTotal = engine.calculateFinalPrice(sportDay, sportSeats, MembershipTier.SILVER, sportPromo);

        System.out.println("=== Payment Summary 3 (Sport Day) ===");
        System.out.println("Event: " + sportDay.getName());
        System.out.println("Seats: " + sportSeats);
        System.out.println("Membership: SILVER");
        System.out.println("Promo: FIXED RM15");
        System.out.println("Final Total: RM " + sportTotal);
    }
}
