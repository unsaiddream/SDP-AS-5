package delivery.model;
import java.util.Objects;
public record Route(String id, Location origin, Location destination, double distanceKm,
                    int estimatedMinutes, double cost, double carbonEmission,
                    boolean available, int trafficLevel, double additionalFees) {
    public Route {
        Checks.text(id, "Route id");
        Objects.requireNonNull(origin, "origin"); Objects.requireNonNull(destination, "destination");
        Checks.nonNegative(distanceKm, "distance"); Checks.nonNegative(estimatedMinutes, "time");
        Checks.nonNegative(cost, "cost"); Checks.nonNegative(carbonEmission, "emissions");
        Checks.nonNegative(additionalFees, "fees");
        if (trafficLevel < 0 || trafficLevel > 5) throw new IllegalArgumentException("Traffic must be 0..5");
    }
}
