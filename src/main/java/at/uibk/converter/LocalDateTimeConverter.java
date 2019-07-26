package at.uibk.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@FacesConverter (value = "localDateTimeConverter")
public class LocalDateTimeConverter implements Converter<LocalDateTime> {
    private static final DateTimeFormatter simpleLocalDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDateTime getAsObject(FacesContext context, UIComponent component, String localDateTime) {
        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (localDateTime == null)
            return null;
        localDateTime = localDateTime.trim();
        if (localDateTime.isEmpty())
            return null;
        try {
            return LocalDateTime.parse(localDateTime, simpleLocalDate);
        } catch (DateTimeParseException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error converting date", "Use format:dd.MM.yyyy"));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDateTime value) {
        return value==null ? "" : value.format(simpleLocalDate);
    }
}
