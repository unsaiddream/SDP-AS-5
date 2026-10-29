package delivery.planner;
import delivery.model.*;
import delivery.strategy.RouteStrategy;
import java.util.*;
public final class RoutePlanner {
    private RouteStrategy strategy;
    public RoutePlanner(RouteStrategy strategy) { setStrategy(strategy); }
    public void setStrategy(RouteStrategy strategy) { this.strategy = Objects.requireNonNull(strategy, "strategy"); }
    public RouteDecision plan(List<Route> routes, DeliveryRequest request) { return strategy.decide(routes, request); }
}
