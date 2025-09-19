package github.kasuminova.mmce.client.gui.util;


public class AnimationValue {
    private static final double EPSILON = 1E-7;

    private final double px3, px2, px1, py3, py2, py1;
    private final int animationTime;

    private long    startTime;
    private boolean finished   = false;
    private float   solveCache = 0;

    private double prevValue;
    private double targetValue;

    public AnimationValue(double value, double targetValue, int animationTime, double a, double b, double c, double d) {
        this.prevValue = value;
        this.targetValue = targetValue;

        this.animationTime = animationTime;
        this.px3 = 3 * a;
        this.px2 = 3 * (c - a) - this.px3;
        this.px1 = 1 - this.px3 - this.px2;
        this.py3 = 3 * b;
        this.py2 = 3 * (d - b) - this.py3;
        this.py1 = 1 - this.py3 - this.py2;
    }

    public static AnimationValue ofFinished(double value, int animationTime, double a, double b, double c, double d) {
        return ofFinished(value, value, animationTime, a, b, c, d);
    }

    public static AnimationValue ofFinished(double value, double targetValue, int animationTime, double a, double b, double c, double d) {
        AnimationValue of = of(value, targetValue, animationTime, a, b, c, d);
        of.finished = true;
        of.solveCache = (float) of.solve(1F);
        return of;
    }

    public static AnimationValue of(double value, int animationTime, double a, double b, double c, double d) {
        return new AnimationValue(value, value, animationTime, a, b, c, d);
    }

    public static AnimationValue of(double value, double targetValue, int animationTime, double a, double b, double c, double d) {
        return new AnimationValue(value, targetValue, animationTime, a, b, c, d);
    }

    public double get() {
        return prevValue + ((this.targetValue - prevValue) * getAnimationPercent());
    }

    public AnimationValue set(double targetValue) {
        this.prevValue = prevValue + ((this.targetValue - prevValue) * getAnimationPercent());
        this.targetValue = targetValue;
        reset();
        return this;
    }

    public AnimationValue setImmediate(double targetValue) {
        this.prevValue = targetValue;
        this.targetValue = targetValue;
        this.finished = true;
        this.solveCache = (float) solve(1F);
        return this;
    }

    public double getPrevValue() {
        return prevValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public double getX(double t) {
        return ((this.px1 * t + this.px2) * t + this.px3) * t;
    }

    public double getY(double t) {
        return ((this.py1 * t + this.py2) * t + this.py3) * t;
    }

    public double solve(double x) {
        if (x == 0 || x == 1) {
            return this.getY(x);
        }
        double t = x;
        for (int i = 0; i < 8; i++) {
            double g = this.getX(t) - x;
            if (Math.abs(g) < EPSILON) {
                return this.getY(t);
            }
            double d = (3 * this.px1 * t + 2 * this.px2) * t + this.px3;
            if (Math.abs(d) < 1E-6) {
                break;
            }
            t = t - g / d;
        }
        return this.getY(t);
    }

    public float getAnimationPercent() {
        if (finished) {
            return solveCache;
        }
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        float timePercent = Math.max(Math.min((float) elapsedTime / animationTime, 1F), 0);

        double result = solve(timePercent);
        if (timePercent >= 1F) {
            finished = true;
            this.solveCache = (float) result;
        }
        return (float) result;
    }

    public boolean isAnimFinished() {
        return finished;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.finished = false;
    }
}
