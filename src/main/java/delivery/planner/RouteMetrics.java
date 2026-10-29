package delivery.planner;
import delivery.model.*;
public record RouteMetrics(double finalMinutes, double totalCost) {
    public static RouteMetrics of(Route route, DeliveryRequest request) {
        double minutes = route.estimatedMinutes() + route.trafficLevel() * 3.0;
        double subtotal = route.cost() + route.distanceKm() * 50 + route.additionalFees();
        double cost = subtotal * (request.priority() == DeliveryPriority.URGENT ? 1.25 : 1.0);
        Checks.nonNegative(minutes, "calculated time"); Checks.nonNegative(cost, "calculated cost");
        return new RouteMetrics(minutes, cost);
    }
}
