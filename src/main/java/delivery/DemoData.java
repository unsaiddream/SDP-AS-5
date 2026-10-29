package delivery;
import delivery.model.*;
import java.util.*;
public final class DemoData {
    private DemoData() {}
    public static final Location ORIGIN = new Location("Warehouse", 51.1694, 71.4491);
    public static final Location DESTINATION = new Location("Customer", 51.1280, 71.4300);
    public static Route route(String id, double km, int minutes, double cost, double co2, boolean available, int traffic, double fees) {
        return new Route(id, ORIGIN, DESTINATION, km, minutes, cost, co2, available, traffic, fees);
    }
    public static List<Route> routes() {
        return List.of(route("A", 10, 15, 2200, 3.5, true, 4, 100),
                route("B", 14, 20, 2800, 4, true, 0, 200),
                route("C", 12, 40, 1200, 0.6, true, 1, 0),
                route("D", 18, 45, 700, 3, true, 0, 0),
                route("E", 8, 30, 1700, 2, true, 2, 100),
                route("F", 9, 10, 100, 0.1, false, 0, 0),
                route("G", 11, 25, 1800, 1.5, true, 1, 50),
                route("H", 20, 18, 6000, 5, true, 0, 500));
    }
    public static DeliveryRequest request(DeliveryPriority priority, double budget, int deadline) {
        return new DeliveryRequest(priority, ORIGIN, DESTINATION, budget, deadline, false, 10, Set.of());
    }
}
