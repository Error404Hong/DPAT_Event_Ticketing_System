package ticketing;

public class Event {
    private final String name;
    private final boolean weekend;
    private final double bookingFee;
    private final boolean isHighDemand;

    public Event(String name, boolean weekend, double bookingFee, boolean isHighDemand) {
        this.name = name;
        this.weekend = weekend;
        this.bookingFee = bookingFee;
        this.isHighDemand = isHighDemand;
    }

    public String getName() {
        return name;
    }

    public boolean isWeekend() {
        return weekend;
    }

    public double getBookingFee() {
        return bookingFee;
    }

    public boolean isHighDemand() { return isHighDemand; }
}

