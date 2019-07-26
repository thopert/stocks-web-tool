package at.uibk.services.email;

import at.uibk.model.mainEntities.StockPriceAlarm;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class EmailNotifier implements Serializable {

    @Resource(name = "mail/gmail")
    private Session session;

    private Configuration configuration;

    @PostConstruct
    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_27);
        configuration.setClassForTemplateLoading(getClass(), "/ftl-templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
    }

    public void sendAlarm(StockPriceAlarm stockPriceAlarm, BigDecimal price) {
        String emailText = getAlarmEmailText(stockPriceAlarm, price);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, stockPriceAlarm.getUser().getEmail());
            message.setSubject("MyPortfolio: Price alarm for " + stockPriceAlarm.getHolding().getStockIdentifier());
            message.setContent(emailText, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Couldn't send email!");
        }
    }

    private String getAlarmEmailText(StockPriceAlarm stockPriceAlarm, BigDecimal price) {
        Map<String, Object> dataModel = new HashMap<>();

        dataModel.put("stockPriceAlarm", stockPriceAlarm);

        dataModel.put("price", price);

        try {
            StringWriter stringWriter = new StringWriter();
            Template template = configuration.getTemplate("template.ftlh");
            template.process(dataModel, stringWriter);
            return stringWriter.toString();
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Email-Templates not found!");
        }
    }
}
