package bookelasticapi1.elasticbook.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import bookelasticapi1.elasticbook.ElkException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CsvFileParser<T> {

    final Class<T> typeParameterClass;

    public CsvFileParser(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public List<T> parse(String filepath) {
        final CsvMapper csvMapper = new CsvMapper();

        final CsvSchema schema = csvMapper
                .typedSchemaFor(typeParameterClass)
                .withHeader()
                .withColumnReordering(true);

        try {
            return csvMapper
                    .readerFor(typeParameterClass)
                    .with(schema)
                    .<T>readValues(new File(filepath))
                    .readAll();
        } catch (IOException e) {
            throw new ElkException(e);
        }
    }

}
