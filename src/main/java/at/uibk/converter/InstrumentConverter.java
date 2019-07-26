package at.uibk.converter;

import at.uibk.model.mainEntities.Instrument;
import at.uibk.services.beans.InstrumentBean;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Instrument.class, managed = true)
public class InstrumentConverter implements Converter<Instrument> {
    @EJB
    private InstrumentBean instrumentBean;

    @Override
    public Instrument getAsObject(FacesContext context, UIComponent component, String instrumentIdString) {
        if(instrumentIdString == null || instrumentIdString.isEmpty()){
            return null;
        }

        instrumentIdString = normalizeInput(instrumentIdString);

        try {
            long instrumentId = Long.parseLong(instrumentIdString);
             return instrumentBean.find(instrumentId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeInput(String input){
        return input.replace(" ", "");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Instrument instrument) {
        return instrument == null ? "" : Long.toString(instrument.getId());
    }
}
