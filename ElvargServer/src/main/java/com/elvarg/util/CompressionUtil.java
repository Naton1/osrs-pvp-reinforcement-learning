package com.elvarg.util;

import com.google.common.cache.CacheLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import static com.google.common.io.ByteStreams.toByteArray;

/**
 * A static-utility class containing containing extension or helper methods for
 * <b>co</b>mpressor-<b>dec</b>compressor<b>'s</b>.
 *
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 */
public final class CompressionUtil {

    /**
     * Suppresses the default-public constructor preventing this class from
     * being instantiated by other classes.
     *
     * @throws UnsupportedOperationException If this class is instantiated
     *                                       within itself.
     */
    private CompressionUtil() {
        throw new UnsupportedOperationException("static-utility classes may not be instantiated.");
    }

    /**
     * Uncompresses a {@code byte} array of g-zipped data.
     *
     * @param data The compressed, g-zipped data.
     * @return The uncompressed data.
     * @throws IOException If some I/O exception occurs.
     */
    public static byte[] gunzip(byte[] data) throws IOException {
        return toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
    }

    /**
     * Uncompresses a {@code byte} array of b-zipped data that does not contain
     * a header.
     * <p>
     * <p>
     * A b-zip header block consists of <tt>2</tt> {@code byte}s, they are
     * replaced with 'h' and '1' as that is what our {@link CacheLoader file
     * system} compresses the header as.
     * </p>
     *
     * @param data   The compressed, b-zipped data.
     * @param offset The offset position of the data.
     * @param length The length of the data.
     * @return The uncompressed data.
     * @throws IOException If some I/O exception occurs.
     */
    public static byte[] unbzip2Headerless(byte[] data, int offset, int length) throws IOException {
        /* Strip the header from the data. */
        byte[] bzip2 = new byte[length + 2];
        bzip2[0] = 'h';
        bzip2[1] = '1';
        System.arraycopy(data, offset, bzip2, 2, length);

		/* Uncompress the headerless data */
        return unbzip2(bzip2);
    }

    /**
     * Uncompresses a {@code byte} array of b-zipped data.
     *
     * @param data The compressed, b-zipped data.
     * @return The uncompressed data.
     * @throws IOException If some I/O exception occurs.
     */
    public static byte[] unbzip2(byte[] data) throws IOException {
        //return toByteArray(new CBZip2InputStream(new ByteArrayInputStream(data)));
        return null;
    }

}
