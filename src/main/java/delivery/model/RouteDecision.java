package delivery.model;
import java.util.List;
public record RouteDecision(String strategyName, Route selectedRoute, double score,
                            String explanation, List<RouteAssessment> assessments) {
    public RouteDecision { assessments = List.copyOf(assessments); }
}
