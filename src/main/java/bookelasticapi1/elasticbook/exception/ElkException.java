package bookelasticapi1.elasticbook.exception;

public class ElkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ElkException(String errorMessage) {
        super(errorMessage);
    }

    public ElkException(Throwable cause) {
        super(cause);
    }

}
