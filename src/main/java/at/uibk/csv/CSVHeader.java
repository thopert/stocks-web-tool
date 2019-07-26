package at.uibk.csv;

import java.io.Serializable;

public class CSVHeader implements Serializable {
    private CSVHeaderType type;

    private String name;

    public CSVHeader(CSVHeaderType type, String name) {
        this.type = type;
        this.name = name;
    }

    public CSVHeaderType getType() {
        return type;
    }

    public void setType(CSVHeaderType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "CSVHeader{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
