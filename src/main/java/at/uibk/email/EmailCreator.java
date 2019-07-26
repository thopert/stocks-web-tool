package at.uibk.email;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;
import at.uibk.model.mainEntities.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EmailCreator {


    public static void main(String[] args) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);

        cfg.setClassForTemplateLoading(EmailCreator.class, "/ftl-templates");

        cfg.setDefaultEncoding("UTF-8");

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);

        cfg.setWrapUncheckedExceptions(true);

        User user = new User();
        user.setFirstName("Thomas");
        user.setLastName("Pertender");

        Portfolio portfolio = new Portfolio();
        portfolio.setName("Test");
        portfolio.setUser(user);

        Holding holding = new Holding();
        holding.setStockIdentifier(new StockIdentifier("AAPL", StockExchange.VIE));
        holding.setName("Apple Inc.");
        holding.setPortfolio(portfolio);

        StockPriceAlarm stockPriceAlarm = new StockPriceAlarm();
        stockPriceAlarm.setType(StockPriceAlarmType.PERCENT_INTERVAL);
        stockPriceAlarm.setReferencePrice(BigDecimal.valueOf(100));
        stockPriceAlarm.setAmount(BigDecimal.valueOf(.1));
        stockPriceAlarm.setHolding(holding);

        StockQuote stockQuote = new StockQuote();
        stockQuote.setPrice(BigDecimal.valueOf(150));

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("stockPriceAlarm", stockPriceAlarm);
        dataModel.put("stockQuote", stockQuote);

        Template template = cfg.getTemplate("template.ftlh");

        Writer out = new OutputStreamWriter(System.out);
        template.process(dataModel, out);

    }

}
