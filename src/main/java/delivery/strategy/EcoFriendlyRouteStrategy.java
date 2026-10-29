package delivery.strategy;
import delivery.model.RouteAssessment;
import java.util.List;
import java.util.function.ToDoubleFunction;
public final class EcoFriendlyRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Eco-friendly"; }
    protected String formula() { return "eco score = CO2 kg * 0.8 + distance km * 0.2"; }
    protected ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible) { return a -> a.route().carbonEmission() * 0.8 + a.route().distanceKm() * 0.2; }
}
