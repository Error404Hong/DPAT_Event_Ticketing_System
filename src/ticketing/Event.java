package ticketing;

public class Event {
    private final String name;
    private final boolean weekend;
    private final double bookingFee;

    public Event(String name, boolean weekend, double bookingFee) {
        this.name = name;
        this.weekend = weekend;
        this.bookingFee = bookingFee;
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
}
