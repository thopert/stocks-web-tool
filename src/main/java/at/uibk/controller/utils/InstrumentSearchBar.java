package at.uibk.controller.utils;

import at.uibk.model.mainEntities.Instrument;
import at.uibk.services.beans.InstrumentBean;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class InstrumentSearchBar implements Serializable {
    private static int MAX_RESULTS = 20;
    private static int MAX_SYMBOL_SIZE = 10;

    @EJB
    private InstrumentBean instrumentBean;

    private String searchTerm;

    private String searchType = "ALL";

    private List<Instrument> instruments;

    private boolean hasSearchTerm() {
        if (searchTerm == null)
            return false;

        searchTerm = searchTerm.trim();

        if (searchTerm.isEmpty())
            return false;

        return true;
    }


    public List<Instrument> search(String searchTerm){
        this.searchTerm = searchTerm;
        search();
        List<Instrument> searchResults = instruments;
        instruments = null;

        return searchResults;
    }

    public void search() {
        if (!hasSearchTerm()) {
            return;
        }

        instruments = null;

        switch (searchType) {
            case "ALL":
                searchAll();
                break;
            case "SYMBOL":
                instruments = instrumentBean.searchBySymbol(searchTerm, MAX_RESULTS);
                break;
            case "NAME":
                instruments = instrumentBean.searchByName(searchTerm, MAX_RESULTS);
                break;
            case "ISIN":
                instruments = instrumentBean.searchByIsin(searchTerm, MAX_RESULTS);
                break;
        }
    }

    private void searchAll() {
        if (searchTerm.length() <= MAX_SYMBOL_SIZE) {
            instruments = instrumentBean.findBySymbol(searchTerm);
        }

        if (instruments == null || instruments.isEmpty())
            instruments = instrumentBean.searchAll(searchTerm, 20);
    }


    /* getters and setters */

    public List<String> getSearchTypes() {
        return Arrays.asList("ALL, SYMBOL, NAME, ISIN, WKN");
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }
}
