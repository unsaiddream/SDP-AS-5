package delivery.model;
public final class Checks {
    private Checks() {}
    public static void nonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
    public static void text(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
    }
}
