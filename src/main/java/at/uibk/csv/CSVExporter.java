package at.uibk.csv;

import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.Trade;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CSVExporter implements Serializable {
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private List<String> getHeaders() {
        return Arrays.asList(
                "symbol",
                "mic",
                "type",
                "date",
                "price",
                "quantity",
                "exchange rate",
                "brokerage",
                "brokerage currency");
    }

    public String fromPortfolio(Portfolio portfolioWithHoldings){
        return fromHoldings(portfolioWithHoldings.getHoldings());
    }

    public String fromHoldings(List<Holding> holdingsWithTrades) {
        StringBuilder csvBuilder = new StringBuilder();

        try (CSVPrinter csvPrinter = CSV_FORMAT.print(csvBuilder)) {
            csvPrinter.printRecord(getHeaders());

            if(holdingsWithTrades.isEmpty()){
                return csvBuilder.toString();
            }

            for (Holding holdingsWithTrade : holdingsWithTrades) {
                List<Trade> trades = holdingsWithTrade.getTrades();

                trades.sort(Comparator.comparing(Trade::getDate));

                for (Trade trade : trades) {
                    if (!trade.isAdjustment()) {
                        List<String> csvRecord = getCsvRecord(trade);

                        csvPrinter.printRecord(csvRecord);
                    }
                }
            }

            return csvBuilder.toString();

        } catch (IOException e) {
            throw new CSVExporterException(e);
        }

    }

    private List<String> getCsvRecord(Trade trade) {
        DecimalFormat fourScaleDf = new DecimalFormat("#0.0000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));


        String symbol = trade.getHolding().getSymbol();
        String mic = trade.getHolding().getStockExchange().getMic();
        String type = trade.getType().toString();
        String date = DATE_TIME_FORMATTER.format(trade.getDate());
        String price =  fourScaleDf.format(trade.getPrice());
        String quantity =  fourScaleDf.format(trade.getQuantity());
        String exchangeRate = fourScaleDf.format(trade.getDefaultExchangeRate());
        String brokerage = fourScaleDf.format(trade.getBrokerage());
        String brokerageCurrency = trade.getBrokerageCurrency().toString();

        return Arrays.asList(symbol, mic, type, date, price, quantity, exchangeRate, brokerage, brokerageCurrency);
    }

}
