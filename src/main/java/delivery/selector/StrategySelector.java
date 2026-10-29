package delivery.selector;
import delivery.model.*;
import delivery.strategy.RouteStrategy;
import java.util.*;
public final class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies;
    public StrategySelector(Map<DeliveryPriority, RouteStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
        if (!this.strategies.keySet().containsAll(EnumSet.allOf(DeliveryPriority.class)))
            throw new IllegalArgumentException("A strategy is required for every priority");
    }
    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request, "request");
        DeliveryPriority key = request.priority() == DeliveryPriority.STANDARD && request.ecoPreferred()
                ? DeliveryPriority.ECO : request.priority();
        return strategies.get(key);
    }
}
