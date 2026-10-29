package delivery.model;
import delivery.planner.RouteMetrics;
import java.util.List;
public record RouteAssessment(Route route, RouteMetrics metrics, List<String> rejectionReasons) {
    public RouteAssessment { rejectionReasons = List.copyOf(rejectionReasons); }
    public boolean eligible() { return rejectionReasons.isEmpty(); }
}
