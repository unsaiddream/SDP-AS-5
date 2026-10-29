package delivery;
import delivery.model.*;
import delivery.planner.*;
import delivery.selector.StrategySelector;
import delivery.strategy.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
class RouteEngineTest {
    private final DeliveryRequest request = DemoData.request(DeliveryPriority.STANDARD, 5000, 60);
    private final List<Route> routes = DemoData.routes();
    private static Stream<RouteStrategy> strategies() {
        return Stream.of(new FastestRouteStrategy(), new CheapestRouteStrategy(), new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(.4, .3, .1, .2), new DistanceFirstStrategy());
    }
    private Route route(String id, double km, int minutes, double cost, double co2, int traffic) {
        return DemoData.route(id, km, minutes, cost, co2, true, traffic, 0);
    }
    private DeliveryRequest limited(double budget, int time, double emissions, Set<String> prohibited) {
        return new DeliveryRequest(DeliveryPriority.STANDARD, DemoData.ORIGIN, DemoData.DESTINATION, budget, time, false, emissions, prohibited);
    }
    @Test void fastestAccountsForTraffic() {
        Route a = route("A", 1, 10, 100, 1, 5), b = route("B", 1, 20, 100, 1, 0);
        assertEquals(b, new FastestRouteStrategy().selectRoute(List.of(a,b), request));
    }
    @Test void cheapestUsesDistanceNotOnlyBaseCost() {
        Route a = route("A", 20, 10, 100, 1, 0), b = route("B", 1, 10, 500, 1, 0);
        assertEquals(b, new CheapestRouteStrategy().selectRoute(List.of(a,b), request));
    }
    @Test void urgentCostIncludesFeesAndSurcharge() {
        Route r = DemoData.route("R", 10, 20, 1000, 1, true, 2, 100);
        RouteMetrics m = RouteMetrics.of(r, DemoData.request(DeliveryPriority.URGENT, 5000, 60));
        assertEquals(2000, m.totalCost()); assertEquals(26, m.finalMinutes());
    }
    @Test void ecoUsesDistanceAndEmission() {
        Route a = route("A", 20, 10, 100, .1, 0), b = route("B", 1, 10, 100, 1, 0);
        RouteDecision d = new EcoFriendlyRouteStrategy().decide(List.of(a,b), request);
        assertEquals(b, d.selectedRoute()); assertEquals(1, d.score(), 1e-9);
    }
    @Test void balancedHasKnownNormalizedScore() {
        Route a = route("A", 10, 10, 100, 1, 0), b = route("B", 20, 20, 100, 0, 0);
        RouteDecision d = new BalancedRouteStrategy(.4,.3,.1,.2).decide(List.of(a,b), request);
        assertEquals(a, d.selectedRoute()); assertEquals(.2, d.score(), 1e-9);
    }
    @Test void balancedWeightsChangeWinner() {
        Route a = route("A", 1, 10, 1000, 1, 0), b = route("B", 1, 20, 100, 1, 0);
        assertEquals(a, new BalancedRouteStrategy(1,0,0,0).selectRoute(List.of(a,b), request));
        assertEquals(b, new BalancedRouteStrategy(0,1,0,0).selectRoute(List.of(a,b), request));
    }
    @Test void balancedSingleCandidateHasZeroScore() {
        assertEquals(0, new BalancedRouteStrategy(.4,.3,.1,.2).decide(List.of(routes.get(0)), request).score());
    }
    @Test void balancedIgnoresRejectedOutliersWhenNormalizing() {
        Route a = route("A", 10, 10, 100, 1, 0), b = route("B", 20, 20, 100, 0, 0);
        Route x = route("X", 1000, 1000, 100000, 1000, 0);
        RouteStrategy s = new BalancedRouteStrategy(.4,.3,.1,.2);
        assertEquals(s.decide(List.of(a,b), request).score(), s.decide(List.of(a,b,x), request).score());
    }
    @Test void invalidWeightsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(.4,.4,.4,.4));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-.1,.5,.3,.3));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.NaN,0,0,1));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(Double.POSITIVE_INFINITY,0,0,0));
    }
    @Test void samePlannerSupportsThreeSwitches() {
        RoutePlanner p = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("B", p.plan(routes,request).selectedRoute().id());
        p.setStrategy(new CheapestRouteStrategy()); assertEquals("D", p.plan(routes,request).selectedRoute().id());
        p.setStrategy(new EcoFriendlyRouteStrategy()); assertEquals("C", p.plan(routes,request).selectedRoute().id());
        p.setStrategy(new DistanceFirstStrategy()); assertEquals("E", p.plan(routes,request).selectedRoute().id());
    }
    @Test void selectorMapsEveryPriorityAndHonorsPrecedence() {
        Map<DeliveryPriority, RouteStrategy> mapping = Map.of(DeliveryPriority.URGENT,new FastestRouteStrategy(),
                DeliveryPriority.ECONOMY,new CheapestRouteStrategy(), DeliveryPriority.ECO,new EcoFriendlyRouteStrategy(),
                DeliveryPriority.STANDARD,new BalancedRouteStrategy(.4,.3,.1,.2));
        StrategySelector selector = new StrategySelector(mapping);
        for (DeliveryPriority priority : DeliveryPriority.values()) assertSame(mapping.get(priority), selector.select(DemoData.request(priority,5000,60)));
        for (DeliveryPriority priority : DeliveryPriority.values()) {
            DeliveryRequest r = new DeliveryRequest(priority, DemoData.ORIGIN, DemoData.DESTINATION,5000,60,true,10,Set.of());
            assertSame(mapping.get(priority == DeliveryPriority.STANDARD ? DeliveryPriority.ECO : priority), selector.select(r));
        }
    }
    @Test void selectorRejectsIncompleteMapping() { assertThrows(IllegalArgumentException.class, () -> new StrategySelector(Map.of())); }
    @ParameterizedTest @MethodSource("strategies") void allStrategiesApplyEveryHardConstraint(RouteStrategy strategy) {
        Route good = route("good", 2, 20, 100, 1, 0);
        List<Route> candidates = List.of(good, DemoData.route("unavailable",0,0,0,0,false,0,0),
                route("budget",0,0,501,0,0),route("late",0,31,0,0,0), route("emission",0,0,0,3,0),
                route("prohibited",0,0,0,0,0), new Route("wrong", DemoData.DESTINATION, DemoData.ORIGIN,0,0,0,0,true,0,0));
        RouteDecision d = strategy.decide(candidates, limited(500,30,2,Set.of("prohibited")));
        assertEquals(good, d.selectedRoute());
        assertEquals(6, d.assessments().stream().filter(a -> !a.eligible()).count());
        assertTrue(d.explanation().contains("Budget exceeded"));
        assertTrue(d.explanation().contains("Delivery deadline exceeded"));
    }
    @Test void budgetChangeChangesWinner() {
        RouteStrategy s = new FastestRouteStrategy();
        assertEquals("B",s.selectRoute(routes,request).id());
        assertEquals("G",s.selectRoute(routes,limited(2500,60,10,Set.of())).id());
    }
    @Test void deadlineIncludesTraffic() {
        Route r = route("R",1,20,100,1,4);
        assertThrows(NoSuitableRouteException.class, () -> new FastestRouteStrategy().decide(List.of(r),limited(5000,30,10,Set.of())));
    }
    @Test void exactConstraintBoundariesAreAllowed() {
        Route r = route("R",2,20,100,1,1);
        assertEquals(r, new FastestRouteStrategy().selectRoute(List.of(r),limited(200,23,1,Set.of())));
    }
    @ParameterizedTest @MethodSource("strategies") void emptyListFailsExplicitly(RouteStrategy s) {
        assertThrows(NoSuitableRouteException.class, () -> s.decide(List.of(),request));
    }
    @ParameterizedTest @MethodSource("strategies") void impossibleRequestFailsExplicitly(RouteStrategy s) {
        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class, () -> s.decide(routes,limited(0,0,0,Set.of())));
        assertTrue(e.getMessage().contains("Budget exceeded"));
    }
    @Test void fifthStrategyMinimizesDistance() { assertEquals("E",new DistanceFirstStrategy().selectRoute(routes,request).id()); }
    @Test void comparisonKeepsDifferentResultsAndDoesNotSwitchPlanner() {
        RoutePlanner p = new RoutePlanner(new FastestRouteStrategy());
        var result = new StrategyComparison().compare(List.of(new FastestRouteStrategy(),new CheapestRouteStrategy()),routes,request);
        assertEquals(List.of("B","D"), result.stream().map(e -> e.decision().orElseThrow().selectedRoute().id()).toList());
        assertEquals("B",p.plan(routes,request).selectedRoute().id());
    }
    @Test void comparisonReportsFailuresForEachStrategy() {
        var result = new StrategyComparison().compare(strategies().toList(),routes,limited(0,0,0,Set.of()));
        assertEquals(5,result.size()); assertTrue(result.stream().allMatch(e -> e.decision().isEmpty() && !e.failure().isBlank()));
    }
    @Test void tiesUseIdIndependentlyOfInputOrder() {
        Route a = route("A",1,10,100,1,0), b = route("B",1,10,100,1,0);
        for (RouteStrategy s : strategies().toList()) assertEquals(a,s.selectRoute(List.of(b,a),request));
    }
    @Test void invalidDomainValuesRejected() {
        assertThrows(IllegalArgumentException.class, () -> route("R",-1,10,100,1,0));
        assertThrows(IllegalArgumentException.class, () -> route("R",1,10,-1,1,0));
        assertThrows(IllegalArgumentException.class, () -> route("R",Double.NaN,10,1,1,0));
        assertThrows(IllegalArgumentException.class, () -> route("R",1,10,1,1,6));
        assertThrows(IllegalArgumentException.class, () -> new Location("X",91,0));
        assertThrows(IllegalArgumentException.class, () -> limited(-1,60,10,Set.of()));
    }
    @Test void missingDataRejected() {
        assertThrows(NullPointerException.class, () -> new FastestRouteStrategy().decide(routes,null));
        assertThrows(NullPointerException.class, () -> new FastestRouteStrategy().decide(null,request));
        assertThrows(NullPointerException.class, () -> new RoutePlanner(null));
        assertThrows(NullPointerException.class, () -> new RoutePlanner(new FastestRouteStrategy()).setStrategy(null));
    }
    @Test void duplicateRouteIdsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FastestRouteStrategy().decide(List.of(routes.get(0),routes.get(0)),request));
    }
    @Test void requestAndDecisionUseDefensiveCopies() {
        Set<String> ids = new HashSet<>(); ids.add("A");
        DeliveryRequest r = limited(5000,60,10,ids); ids.clear();
        assertTrue(r.prohibitedRouteIds().contains("A"));
        RouteDecision d = new FastestRouteStrategy().decide(routes,request);
        assertThrows(UnsupportedOperationException.class, () -> d.assessments().clear());
    }
}
