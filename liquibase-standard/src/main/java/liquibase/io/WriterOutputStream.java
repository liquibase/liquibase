/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package liquibase.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;


/**
 * {@link OutputStream} implementation that transforms a byte stream to a
 * character stream using a specified charset encoding and writes the resulting
 * stream to a {@link Writer}. The stream is transformed using a
 * {@link CharsetDecoder} object, guaranteeing that all charset
 * encodings supported by the JRE are handled correctly.
 * <p>
 * The output of the {@link CharsetDecoder} is buffered using a fixed size buffer.
 * This implies that the data is written to the underlying {@link Writer} in chunks
 * that are no larger than the size of this buffer. By default, the buffer is
 * flushed only when it overflows or when {@link #flush()} or {@link #close()}
 * is called. In general there is therefore no need to wrap the underlying {@link Writer}
 * in a {@link java.io.BufferedWriter}. {@link WriterOutputStream} can also
 * be instructed to flush the buffer after each write operation. In this case, all
 * available data is written immediately to the underlying {@link Writer}, implying that
 * the current position of the {@link Writer} is correlated to the current position
 * of the {@link WriterOutputStream}.
 * <p>
 * {@link WriterOutputStream} implements the inverse transformation of {@link java.io.OutputStreamWriter};
 * in the following example, writing to {@code out2} would have the same result as writing to
 * {@code out} directly (provided that the byte sequence is legal with respect to the
 * charset encoding):
 * <pre>
 * OutputStream out = ...
 * Charset cs = ...
 * OutputStreamWriter writer = new OutputStreamWriter(out, cs);
 * WriterOutputStream out2 = new WriterOutputStream(writer, cs);</pre>
 * {@link WriterOutputStream} implements the same transformation as {@link java.io.InputStreamReader},
 * except that the control flow is reversed: both classes transform a byte stream
 * into a character stream, but {@link java.io.InputStreamReader} pulls data from the underlying stream,
 * while {@link WriterOutputStream} pushes it to the underlying stream.
 * <p>
 * Note that while there are use cases where there is no alternative to using
 * this class, very often the need to use this class is an indication of a flaw
 * in the design of the code. This class is typically used in situations where an existing
 * API only accepts an {@link OutputStream} object, but where the stream is known to represent
 * character data that must be decoded for further use.
 * </p>
 * <p>
 * Instances of {@link WriterOutputStream} are not thread safe.
 * </p>
 *
 * @since 2.0
 */
public class WriterOutputStream extends OutputStream {
    private static final int BUFFER_SIZE = 1024;

    private final Writer writer;
    private final CharsetDecoder decoder;

    private final boolean writeImmediately;

    /**
     * ByteBuffer used as input for the decoder. This buffer can be small
     * as it is used only to transfer the received data to the
     * decoder.
     */
    private final ByteBuffer decoderIn = ByteBuffer.allocate(128);

    /**
     * CharBuffer used as output for the decoder. It should be
     * somewhat larger as we write from this buffer to the
     * underlying Writer.
     */
    private final CharBuffer decoderOut;

    /**
     * Constructs a new {@link WriterOutputStream} that uses the default character encoding and with a default output
     * buffer size of {@value #BUFFER_SIZE} characters. The output buffer will only be flushed when it overflows or when
     * {@link #flush()} or {@link #close()} is called.
     *
     * @param writer the target {@link Writer}
     * @deprecated 2.5 use {@link #WriterOutputStream(Writer, Charset)} instead
     */
    @Deprecated
    public WriterOutputStream(final Writer writer) {
        this(writer, Charset.defaultCharset(), BUFFER_SIZE, false);
    }

    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of {@value #BUFFER_SIZE}
     * characters. The output buffer will only be flushed when it overflows or when {@link #flush()} or {@link #close()}
     * is called.
     *
     * @param writer the target {@link Writer}
     * @param charset the charset encoding
     */
    public WriterOutputStream(final Writer writer, final Charset charset) {
        this(writer, charset, BUFFER_SIZE, false);
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     *
     * @param writer the target {@link Writer}
     * @param charset the charset encoding
     * @param bufferSize the size of the output buffer in number of characters
     * @param writeImmediately If {@code true} the output buffer will be flushed after each
     *                         write operation, i.e. all available data will be written to the
     *                         underlying {@link Writer} immediately. If {@code false}, the
     *                         output buffer will only be flushed when it overflows or when
     *                         {@link #flush()} or {@link #close()} is called.
     */
    public WriterOutputStream(final Writer writer, final Charset charset, final int bufferSize, final boolean writeImmediately) {
        // @formatter:off
        this(writer, charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
                    .replaceWith("?"),
             bufferSize,
             writeImmediately);
        // @formatter:on
    }

    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of {@value #BUFFER_SIZE}
     * characters. The output buffer will only be flushed when it overflows or when {@link #flush()} or {@link #close()}
     * is called.
     *
     * @param writer the target {@link Writer}
     * @param decoder the charset decoder
     * @since 2.1
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder) {
        this(writer, decoder, BUFFER_SIZE, false);
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     *
     * @param writer the target {@link Writer}
     * @param decoder the charset decoder
     * @param bufferSize the size of the output buffer in number of characters
     * @param writeImmediately If {@code true} the output buffer will be flushed after each
     *                         write operation, i.e. all available data will be written to the
     *                         underlying {@link Writer} immediately. If {@code false}, the
     *                         output buffer will only be flushed when it overflows or when
     *                         {@link #flush()} or {@link #close()} is called.
     * @since 2.1
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder, final int bufferSize, final boolean writeImmediately) {
        this.writer = writer;
        this.decoder = decoder;
        this.writeImmediately = writeImmediately;
        decoderOut = CharBuffer.allocate(bufferSize);
    }

    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of {@value #BUFFER_SIZE}
     * characters. The output buffer will only be flushed when it overflows or when {@link #flush()} or {@link #close()}
     * is called.
     *
     * @param writer the target {@link Writer}
     * @param charsetName the name of the charset encoding
     */
    public WriterOutputStream(final Writer writer, final String charsetName) {
        this(writer, charsetName, BUFFER_SIZE, false);
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     *
     * @param writer the target {@link Writer}
     * @param charsetName the name of the charset encoding
     * @param bufferSize the size of the output buffer in number of characters
     * @param writeImmediately If {@code true} the output buffer will be flushed after each
     *                         write operation, i.e. all available data will be written to the
     *                         underlying {@link Writer} immediately. If {@code false}, the
     *                         output buffer will only be flushed when it overflows or when
     *                         {@link #flush()} or {@link #close()} is called.
     */
    public WriterOutputStream(final Writer writer, final String charsetName, final int bufferSize,
                              final boolean writeImmediately) {
        this(writer, Charset.forName(charsetName), bufferSize, writeImmediately);
    }

    /**
     * Close the stream. Any remaining content accumulated in the output buffer
     * will be written to the underlying {@link Writer}. After that
     * {@link Writer#close()} will be called.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        processInput(true);
        flushOutput();
        writer.close();
    }

    /**
     * Flush the stream. Any remaining content accumulated in the output buffer
     * will be written to the underlying {@link Writer}. After that
     * {@link Writer#flush()} will be called.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        flushOutput();
        writer.flush();
    }

    /**
     * Flush the output.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

    /**
     * Decode the contents of the input ByteBuffer into a CharBuffer.
     *
     * @param endOfInput indicates end of input
     * @throws IOException if an I/O error occurs.
     */
    private void processInput(final boolean endOfInput) throws IOException {
        // Prepare decoderIn for reading
        decoderIn.flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (coderResult.isOverflow()) {
                flushOutput();
            } else if (coderResult.isUnderflow()) {
                break;
            } else {
                // The decoder is configured to replace malformed input and unmappable characters,
                // so we should not get here.
                throw new IOException("Unexpected coder result");
            }
        }
        // Discard the bytes that have been read
        decoderIn.compact();
    }

    /**
     * Write bytes from the specified byte array to the stream.
     *
     * @param b the byte array containing the bytes to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Write bytes from the specified byte array to the stream.
     *
     * @param b the byte array containing the bytes to write
     * @param off the start offset in the byte array
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int c = Math.min(len, decoderIn.remaining());
            decoderIn.put(b, off, c);
            processInput(false);
            len -= c;
            off += c;
        }
        if (writeImmediately) {
            flushOutput();
        }
    }

    /**
     * Write a single byte to the stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
        write(new byte[] {(byte) b}, 0, 1);
    }
}
