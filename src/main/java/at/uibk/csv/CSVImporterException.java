package at.uibk.csv;

public class CSVImporterException extends RuntimeException {
    public CSVImporterException(String message) {
        super(message);
    }

    public CSVImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSVImporterException(Throwable cause) {
        super(cause);
    }

    protected CSVImporterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
