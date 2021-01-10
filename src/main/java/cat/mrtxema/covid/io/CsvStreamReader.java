package cat.mrtxema.covid.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvStreamReader<T> {
    private final String columnSeparator;
    private final Function<Map<String, String>, T> rowReader;

    public CsvStreamReader(String columnSeparator, Function<Map<String, String>, T> rowReader) {
        this.columnSeparator = columnSeparator;
        this.rowReader = rowReader;
    }

    public Stream<T> readAll(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        Map<String, Integer> columnNames = processHeader(reader.readLine().split(columnSeparator));
        return reader.lines()
                .map(line -> line.split(columnSeparator))
                .map(cells -> mapCells(columnNames, cells))
                .map(row -> rowReader.apply(row));
    }

    private Map<String, Integer> processHeader(String[] columnNames) {
        Map<String, Integer> result = new HashMap<>();
        for (int i=0; i<columnNames.length; i++) {
            result.put(columnNames[i], i);
        }
        return result;
    }

    private Map<String, String> mapCells(Map<String, Integer> columnNames, String[] cells) {
        return columnNames.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> cells[entry.getValue()]));
    }
}
