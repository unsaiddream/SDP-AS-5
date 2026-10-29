package delivery.strategy;
import delivery.model.RouteAssessment;
import java.util.List;
import java.util.function.ToDoubleFunction;
public final class CheapestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Cheapest"; }
    protected String formula() { return "cost = (base + distance * 50 + fees) * priority multiplier (URGENT 1.25, otherwise 1)"; }
    protected ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible) { return a -> a.metrics().totalCost(); }
}
