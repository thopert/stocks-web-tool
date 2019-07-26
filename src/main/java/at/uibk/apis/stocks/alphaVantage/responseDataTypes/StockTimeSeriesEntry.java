package at.uibk.apis.stocks.alphaVantage.responseDataTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StockTimeSeriesEntry implements Serializable {
	private LocalDate date;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal adjustedClose;
	private BigDecimal dividendAmount;
	private BigDecimal splitCoefficient;
	private long volume;

	public StockTimeSeriesEntry(LocalDate date ,BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {

		this(date, open, high, low, close, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, volume);
	}

	public StockTimeSeriesEntry(LocalDate date ,BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
								BigDecimal adjustedClose, BigDecimal dividendAmount, BigDecimal splitCoefficient,
								long volume) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.adjustedClose = adjustedClose;
		this.dividendAmount = dividendAmount;
		this.splitCoefficient = splitCoefficient;
		this.volume = volume;
	}
	
	public BigDecimal getOpen() {
		return open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public BigDecimal getClose() {
		return close;
	}

	public long getVolume() {
		return volume;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getAdjustedClose() {
		return adjustedClose;
	}

	public BigDecimal getDividendAmount() {
		return dividendAmount;
	}

	public BigDecimal getSplitCoefficient() {
		return splitCoefficient;
	}

	public boolean hasDividend(){
		return dividendAmount.compareTo(BigDecimal.ZERO) != 0;
	}

	public boolean isAdjustment(){
		return splitCoefficient.compareTo(BigDecimal.ONE) != 0;
	}

	public BigDecimal getChange(){
	    return close.subtract(open);
    }

    public BigDecimal getPercentChange(){
	    return close.divide(open, 4, BigDecimal.ROUND_HALF_EVEN).subtract(BigDecimal.ONE);
    }

	public BigDecimal getConvertedPercentChange(){
		return getPercentChange().multiply(BigDecimal.valueOf(100));
	}

    public BigDecimal getRange(){
		return high.subtract(low);
	}

	@Override
	public String toString() {
		return "\n" + "TimeSeriesEntry [open=" + open + ", high=" + high + ", low=" + low
				+ ", close=" + close + ", volume=" + volume + "]";
	}
	
}
