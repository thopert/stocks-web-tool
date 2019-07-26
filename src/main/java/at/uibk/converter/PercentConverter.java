package at.uibk.converter;

import javax.faces.convert.FacesConverter;
import javax.faces.convert.NumberConverter;

@FacesConverter(value = "at.uibk.converter.PercentConverter")
public class PercentConverter extends NumberConverter {

    public PercentConverter() {
        setType("percent");
        setMaxFractionDigits(2);
    }
}
