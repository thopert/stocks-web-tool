package at.uibk.csv;

public class EmptyCSVFileException extends CSVImporterException {
    public EmptyCSVFileException(String message) {
        super(message);
    }

    public EmptyCSVFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyCSVFileException(Throwable cause) {
        super(cause);
    }

    protected EmptyCSVFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
