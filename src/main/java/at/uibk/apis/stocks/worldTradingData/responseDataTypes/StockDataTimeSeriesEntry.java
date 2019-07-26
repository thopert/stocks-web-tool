package at.uibk.apis.stocks.worldTradingData.responseDataTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StockDataTimeSeriesEntry implements Serializable {
	private LocalDate date;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private long volume;


	public StockDataTimeSeriesEntry(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
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
		return "StockDataTimeSeriesEntry{" +
				"date=" + date +
				", open=" + open +
				", high=" + high +
				", low=" + low +
				", close=" + close +
				", volume=" + volume +
				'}';
	}
}
