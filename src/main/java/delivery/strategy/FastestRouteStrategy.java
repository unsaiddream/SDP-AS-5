package delivery.strategy;
import delivery.model.RouteAssessment;
import java.util.List;
import java.util.function.ToDoubleFunction;
public final class FastestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Fastest"; }
    protected String formula() { return "time = estimated minutes + traffic level * 3"; }
    protected ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible) { return a -> a.metrics().finalMinutes(); }
}
