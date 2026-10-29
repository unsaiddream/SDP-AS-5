package delivery.strategy;
import delivery.model.RouteAssessment;
import java.util.List;
import java.util.function.ToDoubleFunction;
public final class DistanceFirstStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Distance-first"; }
    protected String formula() { return "score = distance in km"; }
    protected ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible) {
        return a -> a.route().distanceKm();
    }
}
