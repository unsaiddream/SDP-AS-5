package delivery.planner;
import delivery.model.*;
import delivery.strategy.RouteStrategy;
import java.util.*;
public final class StrategyComparison {
    public record Entry(String strategy, Optional<RouteDecision> decision, String failure) {}
    public List<Entry> compare(List<RouteStrategy> strategies, List<Route> routes, DeliveryRequest request) {
        return strategies.stream().map(strategy -> {
            try { return new Entry(strategy.getStrategyName(), Optional.of(strategy.decide(routes, request)), ""); }
            catch (NoSuitableRouteException e) { return new Entry(strategy.getStrategyName(), Optional.<RouteDecision>empty(), e.getMessage()); }
        }).toList();
    }
}
