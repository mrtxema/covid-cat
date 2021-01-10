package cat.mrtxema.covid.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipStreamReader<T> {
    private final Predicate<ZipEntry> entryFilter;
    private final Function<InputStream, T> entryReader;

    public ZipStreamReader(Predicate<ZipEntry> entryFilter, Function<InputStream, T> entryReader) {
        this.entryFilter = entryFilter;
        this.entryReader = entryReader;
    }

    public Optional<T> readAny(InputStream inputStream) throws IOException {
        Iterator<T> singletonIteratorResult = readN(inputStream, 1).values().iterator();
        return singletonIteratorResult.hasNext() ? Optional.of(singletonIteratorResult.next()) : Optional.empty();
    }

    public Map<String, T> readAll(InputStream inputStream) throws IOException {
        return readN(inputStream, Integer.MAX_VALUE);
    }

    public Map<String, T> readN(InputStream inputStream, int entriesLimit) throws IOException {
        Map<String, T> result = new HashMap<>();
        try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry = zipStream.getNextEntry();
            int entriesAccepted = 0;
            while (zipEntry != null && (entriesAccepted < entriesLimit)) {
                if (!zipEntry.isDirectory() && entryFilter.test(zipEntry)) {
                    result.put(zipEntry.getName(), entryReader.apply(zipStream));
                    entriesAccepted++;
                }
                zipEntry = zipStream.getNextEntry();
            }
            zipStream.closeEntry();
        }
        return result;
    }
}
