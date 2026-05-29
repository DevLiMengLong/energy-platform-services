package io.github.energyiot.data.access.cleaning;

public final class CleanQualityCode {

    public static final int NORMAL = 0;
    public static final int FORMAT_ERROR = 1;
    public static final int TIME_ERROR = 2;
    public static final int DUPLICATE = 4;
    public static final int OUT_OF_ORDER = 5;
    public static final int OUT_OF_RANGE = 6;
    public static final int SPIKE = 7;
    public static final int CUMULATIVE_ROLLBACK = 8;
    public static final int FORMULA_ERROR = 9;
    public static final int ROLLOVER = 10;

    private CleanQualityCode() {
    }
}
