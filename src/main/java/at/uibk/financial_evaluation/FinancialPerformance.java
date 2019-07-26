package at.uibk.financial_evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FinancialPerformance {

    LocalDate getDeadLine();

    BigDecimal getCurrentValue();

    BigDecimal getTotalSales();

    BigDecimal getTotalDividends();

    BigDecimal getTotalCosts();

    BigDecimal getTotalEarnings();

    BigDecimal getTotalReturn();

    BigDecimal getTotalPercentReturn();
}
