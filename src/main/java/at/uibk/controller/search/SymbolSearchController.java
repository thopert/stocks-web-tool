package at.uibk.controller.search;

import at.uibk.model.mainEntities.Instrument;
import at.uibk.services.beans.InstrumentBean;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@ViewScoped
@Named
public class SymbolSearchController implements Serializable {
    public static final String KEYWORDS = "keywords";
    public static final String ISIN = "isin";
    public static final String WKN = "wkn";
    public static final String SYMBOL = "symbol";

    @EJB
    private InstrumentBean instrumentBean;

    private String searchCriteria = KEYWORDS;

    private String searchString;

    private List<Instrument> matchedInstruments;

    public void search(){
        switch (searchCriteria){
            case KEYWORDS:
                matchedInstruments = instrumentBean.findByName(searchString);
                break;
            case SYMBOL:
                matchedInstruments = instrumentBean.findBySymbol(searchString);
                break;
            case ISIN:
                matchedInstruments = instrumentBean.findByIsin(searchString);
                break;
            case WKN:
                matchedInstruments = instrumentBean.findByWkn(searchString);
                break;
        }
    }

    public List<Instrument> getMatchedInstruments() {
        return matchedInstruments;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
}
