package at.uibk.csv;

public class CSVExporterException extends RuntimeException {
    public CSVExporterException(String message) {
        super(message);
    }

    public CSVExporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSVExporterException(Throwable cause) {
        super(cause);
    }
}
