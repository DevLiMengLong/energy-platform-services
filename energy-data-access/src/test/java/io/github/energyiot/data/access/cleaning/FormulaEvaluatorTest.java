package io.github.energyiot.data.access.cleaning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormulaEvaluatorTest {

    private final FormulaEvaluator evaluator = new FormulaEvaluator();

    @Test
    void evaluatesSimpleArithmeticFormula() {
        assertThat(evaluator.evaluate("(x - 4) * 25", new BigDecimal("8")))
                .isEqualByComparingTo("100");
    }

    @Test
    void rejectsUnsupportedSymbols() {
        assertThatThrownBy(() -> evaluator.evaluate("sqrt(x)", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
