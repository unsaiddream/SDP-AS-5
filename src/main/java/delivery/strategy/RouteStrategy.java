package delivery.strategy;
import delivery.model.*;
import java.util.List;
public interface RouteStrategy {
    RouteDecision decide(List<Route> routes, DeliveryRequest request);
    String getStrategyName();
    default Route selectRoute(List<Route> routes, DeliveryRequest request) {
        return decide(routes, request).selectedRoute();
    }
}
