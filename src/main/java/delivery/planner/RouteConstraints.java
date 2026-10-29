package delivery.planner;
import delivery.model.*;
import java.util.*;
public final class RouteConstraints {
    private RouteConstraints() {}
    public static List<String> violations(Route route, DeliveryRequest request, RouteMetrics metrics) {
        List<String> reasons = new ArrayList<>();
        if (!route.available()) reasons.add("Route unavailable");
        if (!route.origin().equals(request.origin()) || !route.destination().equals(request.destination())) reasons.add("Origin/destination mismatch");
        if (metrics.totalCost() > request.maxBudget()) reasons.add("Budget exceeded");
        if (metrics.finalMinutes() > request.maxDeliveryMinutes()) reasons.add("Delivery deadline exceeded");
        if (route.carbonEmission() > request.maxEmission()) reasons.add("Emission limit exceeded");
        if (request.prohibitedRouteIds().contains(route.id())) reasons.add("Route prohibited");
        return List.copyOf(reasons);
    }
}
