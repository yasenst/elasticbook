package bookelasticapi1.elasticbook.model;

public enum Subject {
    ALGORITHMS("Algorithms"),
    COMPUTER_SCIENCE("Computer Science"),
    DATA_SCIENCE("Data Science"),
    DATABASES("Databases"),
    PROGRAMMING("Programming"),
    SIGNAL_PROCESSING("Signal Processing");

    public final String value;

    Subject(String value) {
        this.value = value;
    }
}
