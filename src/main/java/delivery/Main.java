package delivery;
import delivery.model.*;
import delivery.planner.*;
import delivery.selector.StrategySelector;
import delivery.strategy.*;
import java.util.*;
public final class Main {
    public static void main(String[] args) {
        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy eco = new EcoFriendlyRouteStrategy();
        RouteStrategy balanced = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        List<RouteStrategy> strategies = List.of(fastest, cheapest, eco, balanced);
        List<Route> routes = DemoData.routes();
        DeliveryRequest request = DemoData.request(DeliveryPriority.STANDARD, 5000, 60);
        RoutePlanner planner = new RoutePlanner(fastest);
        System.out.println("RUNTIME SWITCHING (same planner, three switches)");
        print(planner.plan(routes, request));
        for (RouteStrategy strategy : List.of(cheapest, eco, balanced)) {
            planner.setStrategy(strategy); print(planner.plan(routes, request));
        }
        StrategySelector selector = new StrategySelector(Map.of(DeliveryPriority.URGENT, fastest,
                DeliveryPriority.ECONOMY, cheapest, DeliveryPriority.ECO, eco, DeliveryPriority.STANDARD, balanced));
        System.out.println("AUTOMATIC SELECTION / DYNAMIC REQUESTS");
        for (DeliveryRequest r : List.of(DemoData.request(DeliveryPriority.URGENT, 10000, 30),
                DemoData.request(DeliveryPriority.ECONOMY, 1700, 60), DemoData.request(DeliveryPriority.ECO, 5000, 60))) {
            planner.setStrategy(selector.select(r)); print(planner.plan(routes, r));
        }
        System.out.println("COMPARISON / EXPLANATIONS (same request)");
        for (StrategyComparison.Entry entry : new StrategyComparison().compare(strategies, routes, request)) {
            entry.decision().ifPresentOrElse(d -> { print(d); System.out.println(d.explanation()); }, () -> System.out.println(entry.failure()));
        }
        System.out.println("EXPECTED FAILURE CASES");
        failure(() -> fastest.decide(List.of(), request));
        failure(() -> fastest.decide(routes, DemoData.request(DeliveryPriority.STANDARD, 0, 0)));
        failure(() -> new BalancedRouteStrategy(1, 1, 1, 1));
    }
    private static void print(RouteDecision d) {
        RouteMetrics m = d.assessments().stream().filter(a -> a.route().equals(d.selectedRoute())).findFirst().orElseThrow().metrics();
        System.out.printf(Locale.ROOT, "%s -> %s | time %.0f min | cost %.2f KZT | score %.4f%n", d.strategyName(), d.selectedRoute().id(), m.finalMinutes(), m.totalCost(), d.score());
    }
    private static void failure(Runnable action) {
        try { action.run(); } catch (NoSuitableRouteException | IllegalArgumentException e) { System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage()); }
    }
}
