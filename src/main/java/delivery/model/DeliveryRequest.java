package delivery.model;
import java.util.Objects;
import java.util.Set;
public record DeliveryRequest(DeliveryPriority priority, Location origin, Location destination,
                              double maxBudget, int maxDeliveryMinutes, boolean ecoPreferred,
                              double maxEmission, Set<String> prohibitedRouteIds) {
    public DeliveryRequest {
        Objects.requireNonNull(priority, "priority"); Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Checks.nonNegative(maxBudget, "budget"); Checks.nonNegative(maxDeliveryMinutes, "deadline");
        Checks.nonNegative(maxEmission, "emission limit");
        prohibitedRouteIds = Set.copyOf(prohibitedRouteIds);
    }
}
