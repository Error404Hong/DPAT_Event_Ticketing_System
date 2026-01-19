import ticketing.Event;
import ticketing.Promo;
import ticketing.SeatZone;
import ticketing.MembershipTier;
import ticketing.PromoType;

import java.util.ArrayList;
import java.util.List;

class PricingServices {

    public double calculateFinalPrice(Event event, List<SeatZone> zoneList, MembershipTier tier, Promo promo) {

        if (zoneList == null || zoneList.isEmpty()) {
            throw new IllegalArgumentException("Seat selection cannot be empty.");
        }

        double subtotal = 0.0;

        // 1) Calculate per-seat price and sum
        for (SeatZone zone : zoneList) {
            double seatPrice;

            // Base seat price
            switch (zone) {
                case VIP -> seatPrice = 180.0;
                case STANDARD -> seatPrice = 80.0;
                case BALCONY -> seatPrice = 60.0;
                default -> throw new IllegalArgumentException("Unknown seat zone");
            }

            // Weekend surcharge per seat
            if (event.isWeekend()) {
                seatPrice *= 1.10; // +10%
            }

            subtotal += seatPrice;
        }

        // 2) Booking fee applied once
        subtotal += event.getBookingFee();

        // 3) Membership discount
        switch (tier) {
            case GOLD -> subtotal *= 0.90;   // -10%
            case SILVER -> subtotal *= 0.95; // -5%
            case NONE -> {
                // no discount
            }
            default -> throw new IllegalArgumentException("Unknown membership tier");
        }

        // 4) Promo discount
        if (promo != null && promo.getType() != PromoType.NONE) {
            switch (promo.getType()) {
                case PERCENT -> subtotal *= (1.0 - promo.getValue() / 100.0);
                case FIXED -> subtotal -= promo.getValue();
                case NONE -> {
                    // no promo
                }
                default -> throw new IllegalArgumentException("Unknown promo type");
            }
        }

        // Prevent negative totals
        if (subtotal < 0) subtotal = 0;

        // Round to 2 decimal places
        return Math.round(subtotal * 100.0) / 100.0;
    }
}

public class SimpleEventTicketing {
    public static void main(String[] args) {

        PricingServices pricing = new PricingServices();

        // 1) Concert – Weekend + GOLD + 10% Promo
        Event concert = new Event("Campus Concert", true, 5.00);

        List<SeatZone> selectedSeats = new ArrayList<>();
        selectedSeats.add(SeatZone.VIP);
        selectedSeats.add(SeatZone.VIP);
        selectedSeats.add(SeatZone.STANDARD);

        Promo promo = new Promo(PromoType.PERCENT, 10.0);

        double total = pricing.calculateFinalPrice(
                concert, selectedSeats, MembershipTier.GOLD, promo);

        System.out.println("=== Payment Summary 1 ===");
        System.out.println("Event: " + concert.getName());
        System.out.println("Selected Seats: " + selectedSeats);
        System.out.println("Membership: GOLD");
        System.out.println("Promo: PERCENT (10%)");
        System.out.println("Final Total: RM " + total);

        // 2) Movie Night – Weekday + NO discount
        Event movieNight = new Event("Movie Night", false, 2.00);
        List<SeatZone> seats2 =
                List.of(SeatZone.STANDARD, SeatZone.STANDARD, SeatZone.STANDARD);

        double total2 = pricing.calculateFinalPrice(
                movieNight, seats2, MembershipTier.NONE, null);

        System.out.println();
        System.out.println("=== Payment Summary 2 ===");
        System.out.println("Event: " + movieNight.getName());
        System.out.println("Selected Seats: " + seats2);
        System.out.println("Membership: NONE");
        System.out.println("Promo: NONE");
        System.out.println("Final Total: RM " + total2);

        // 3) Sport Day – Weekend + SILVER + RM15 FIXED Promo
        Event sportDay = new Event("Campus Sport Day", true, 5.00);
        Promo promoFixed15 = new Promo(PromoType.FIXED, 15.0);
        List<SeatZone> seats3 =
                List.of(SeatZone.BALCONY, SeatZone.BALCONY, SeatZone.BALCONY);

        double total3 = pricing.calculateFinalPrice(
                sportDay, seats3, MembershipTier.SILVER, promoFixed15);

        System.out.println();
        System.out.println("=== Payment Summary 3 ===");
        System.out.println("Event: " + sportDay.getName());
        System.out.println("Selected Seats: " + seats3);
        System.out.println("Membership: SILVER");
        System.out.println("Promo: FIXED (RM15)");
        System.out.println("Final Total: RM " + total3);
    }
}
