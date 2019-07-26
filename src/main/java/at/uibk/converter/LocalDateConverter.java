package at.uibk.converter;

import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;
import java.time.LocalDate;

@FacesConverter(forClass = LocalDate.class)
public class LocalDateConverter extends DateTimeConverter  {

    public LocalDateConverter() {
        setType("localDate");
        setPattern("dd.MM.yyyy");
    }
}
