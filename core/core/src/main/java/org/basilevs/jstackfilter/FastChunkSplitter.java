package org.basilevs.jstackfilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A fast, lazy spliterator for splitting a Reader into chunks separated by blank lines.
 * This is a high-performance replacement for Scanner with a regex delimiter.
 */
class ChunkSpliterator extends Spliterators.AbstractSpliterator<String> {

    private final BufferedReader bufferedReader;

    public ChunkSpliterator(Reader reader) {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
        this.bufferedReader = (reader instanceof BufferedReader)
            ? (BufferedReader) reader
            : new BufferedReader(reader);
    }

    /**
     * Tries to advance and emit the next chunk.
     * A chunk is a sequence of non-empty lines, separated by one or more empty lines.
     */
    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        try {
            String line;
            
            // 1. Skip any leading blank lines from the previous chunk.
            while ((line = bufferedReader.readLine()) != null && line.isEmpty()) {
                // Keep reading until a non-empty line or EOF is found.
            }

            if (line == null) {
                return false; // Reached the end of the stream.
            }

            // 2. We have the first line of a new chunk.
            StringBuilder chunkBuilder = new StringBuilder();
            chunkBuilder.append(line);

            // 3. Read subsequent lines belonging to the same chunk.
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                chunkBuilder.append('\n').append(line);
            }

            // 4. We've found a complete chunk.
            action.accept(chunkBuilder.toString());
            return true;

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BufferedReader getReader() {
        return this.bufferedReader;
    }
}

public class FastChunkSplitter {

    /**
     * Lazily splits a Reader's content into a Stream of chunks.
     * Chunks are separated by one or more blank lines.
     *
     * @param reader The reader providing the text data.
     * @return A Stream of strings, where each string is a chunk of text.
     */
    public static Stream<String> splitToChunks(Reader reader) {
        ChunkSpliterator spliterator = new ChunkSpliterator(reader);

        return StreamSupport.stream(spliterator, false)
            .onClose(() -> {
                try {
                    spliterator.getReader().close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }
}