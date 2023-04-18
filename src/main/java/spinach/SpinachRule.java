package spinach;

public class SpinachRule {

    private static abstract class Evaluator {
        public abstract Object evaluate(String st);
    }

    private static class Evaluators {
        public static IntegerEvaluator ie = new IntegerEvaluator();
        public static DoubleEvaluator de = new DoubleEvaluator();
        public static StringEvaluator se = new StringEvaluator();

        public static class IntegerEvaluator extends Evaluator {
            @Override
            public Object evaluate(String st) {
                return Integer.valueOf(st);
            }
        }

        public static class DoubleEvaluator extends Evaluator {
            @Override
            public Object evaluate(String st) {
                return Double.valueOf(st);
            }
        }

        public static class StringEvaluator extends Evaluator {
            public Object evaluate(String st) {
                return st;
            }
        }
    }

    public String key;

    private Evaluator type;

    public SpinachRule(String key, String type) {
        this.key = key;

        switch (type) {
            case "int", "integer" -> this.type = Evaluators.ie;
            case "double" -> this.type = Evaluators.de;
            case "string" -> this.type = Evaluators.se;
        }
    }

    public Object match(String data) {
        try {
            return type.evaluate(data);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
