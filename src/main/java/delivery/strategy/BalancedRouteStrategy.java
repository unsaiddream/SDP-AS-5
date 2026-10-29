package delivery.strategy;
import delivery.model.RouteAssessment;
import java.util.*;
import java.util.function.ToDoubleFunction;
public final class BalancedRouteStrategy extends ScoredRouteStrategy {
    private final double[] weights;
    public BalancedRouteStrategy(double time, double cost, double distance, double emission) {
        weights = new double[]{time, cost, distance, emission};
        for (double weight : weights) if (!Double.isFinite(weight) || weight < 0 || weight > 1)
            throw new IllegalArgumentException("Weights must be finite and within [0,1]");
        if (Math.abs(Arrays.stream(weights).sum() - 1) > 1e-9) throw new IllegalArgumentException("Weights must sum to 1");
    }
    public String getStrategyName() { return "Balanced"; }
    protected String formula() { return "sum(weight * (value-min)/(max-min)); constant criterion = 0; feasible candidates only; weights=" + Arrays.toString(weights); }
    private double[] values(RouteAssessment a) {
        return new double[]{a.metrics().finalMinutes(), a.metrics().totalCost(), a.route().distanceKm(), a.route().carbonEmission()};
    }
    protected ToDoubleFunction<RouteAssessment> scoring(List<RouteAssessment> eligible) {
        double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] max = new double[4];
        for (RouteAssessment a : eligible) {
            double[] v = values(a);
            for (int i = 0; i < 4; i++) { min[i] = Math.min(min[i], v[i]); max[i] = Math.max(max[i], v[i]); }
        }
        return a -> {
            double[] v = values(a); double score = 0;
            for (int i = 0; i < 4; i++) if (max[i] != min[i]) score += weights[i] * (v[i] - min[i]) / (max[i] - min[i]);
            return score;
        };
    }
}
