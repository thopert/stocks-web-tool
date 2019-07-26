package at.uibk.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named
public class ConverterFactory {

    public LocalDateConverter getLocalDateConverter(){
        return new LocalDateConverter();
    }
}
