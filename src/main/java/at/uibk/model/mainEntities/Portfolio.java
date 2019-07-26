package at.uibk.model.mainEntities;

import at.uibk.model.Currency;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "portfolio")
@NamedEntityGraph(name = "portfolioWithHoldings",
        attributeNodes = @NamedAttributeNode(value = "holdings", subgraph = "holdings"),
        subgraphs = @NamedSubgraph(name = "holdings", attributeNodes = {@NamedAttributeNode("trades"), @NamedAttributeNode("dividends")}))
public class Portfolio implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein!")
    private String name;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "portfolio")
    private List<Holding> holdings;


    public Portfolio() {
    }

    public Portfolio(User user) {
        this.user = user;
    }

    public Portfolio(User user, Currency currency){
        this.user = user;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", currency=" + currency +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return id.equals(portfolio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addHolding(Holding holding) {
        if (holding == null)
            return;

        if (holdings == null) {
            holdings = new ArrayList<>();
        }

        holdings.add(holding);
    }

    public void remove(Holding holding) {
        if (holdings == null || holding == null)
            return;

        holdings.remove(holding);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<Holding> getHoldings() {
        if (holdings == null)
            return new ArrayList<>();

        return holdings;
    }

    public boolean hasHoldings() {
        return holdings != null && !holdings.isEmpty();
    }

    public boolean hasHoldings(boolean withClosed){
        if(withClosed)
            return hasHoldings();

        return !getHoldingsNotClosed().isEmpty();
    }

    public List<Holding> getHoldingsNotClosed() {
        if (holdings == null) return new ArrayList<>();

        return holdings.stream()
                .filter(holding -> !holding.isClosed())
                .collect(Collectors.toList());
    }

    public LocalDate getFirstTradeDate() {
        if (holdings == null) return null;

        return holdings.stream()
                .flatMap(holding -> holding.getTrades().stream())
                .map(Trade::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("Holdings without trades exist!"));
    }

    public int getNumberOfHoldings(){
        return getHoldings().size();
    }

    public List<String> getFullyQualifiedSymbols() {
        return holdings.stream()
                .map(Holding::getFullyQualifiedSymbol)
                .collect(Collectors.toList());
    }
}
