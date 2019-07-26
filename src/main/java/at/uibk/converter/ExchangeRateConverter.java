package at.uibk.converter;

import javax.faces.convert.FacesConverter;
import javax.faces.convert.NumberConverter;

@FacesConverter(value = "at.uibk.converter.ExchangeRateConverter")
public class ExchangeRateConverter extends NumberConverter {

    public ExchangeRateConverter() {
        setMaxFractionDigits(4);
    }
}
