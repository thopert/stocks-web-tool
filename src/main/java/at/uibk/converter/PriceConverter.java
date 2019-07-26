package at.uibk.converter;

import javax.faces.convert.FacesConverter;
import javax.faces.convert.NumberConverter;

@FacesConverter(value = "at.uibk.converter.PriceConverter")
public class PriceConverter extends NumberConverter {

    public PriceConverter() {
        setMaxFractionDigits(2);
    }
}
