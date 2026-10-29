package delivery.strategy;
import delivery.model.*;
import delivery.planner.*;
import java.util.*;
import java.util.function.ToDoubleFunction;
public abstract class ScoredRouteStrategy implements RouteStrategy {
    protected abstract ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible);
    protected abstract String formula();
    @Override public final RouteDecision decide(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(request, "request");
        List<Route> input = List.copyOf(routes);
        Set<String> ids = new HashSet<>();
        for (Route route : input) if (!ids.add(route.id())) throw new IllegalArgumentException("Duplicate route id: " + route.id());
        List<RouteAssessment> assessments = input.stream().map(route -> {
            RouteMetrics metrics = RouteMetrics.of(route, request);
            return new RouteAssessment(route, metrics, RouteConstraints.violations(route, request, metrics));
        }).toList();
        List<RouteAssessment> eligible = assessments.stream().filter(RouteAssessment::eligible).toList();
        if (eligible.isEmpty()) {
            String reasons = assessments.isEmpty() ? "Empty route list" : assessments.toString();
            throw new NoSuitableRouteException(getStrategyName() + ": no suitable route. " + reasons);
        }
        ToDoubleFunction<RouteAssessment> score = scoring(eligible);
        RouteAssessment winner = eligible.stream().min(Comparator.comparingDouble(score)
                .thenComparing(a -> a.route().id())).orElseThrow();
        StringBuilder explanation = new StringBuilder(formula()).append("; lower is better; ties use route id.\n");
        for (RouteAssessment a : assessments) {
            explanation.append(a.route().id()).append(": ");
            if (a.eligible()) explanation.append(String.format(Locale.ROOT,
                    "score=%.4f; time=%d + traffic %d = %.0f min; total cost=%.2f; distance=%.1f km; CO2=%.2f kg",
                    score.applyAsDouble(a), a.route().estimatedMinutes(), a.route().trafficLevel() * 3,
                    a.metrics().finalMinutes(), a.metrics().totalCost(), a.route().distanceKm(), a.route().carbonEmission()));
            else explanation.append("REJECTED: ").append(String.join(", ", a.rejectionReasons()));
            explanation.append('\n');
        }
        return new RouteDecision(getStrategyName(), winner.route(), score.applyAsDouble(winner), explanation.toString(), assessments);
    }
}
