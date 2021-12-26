package com.runescape.cache;


import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public final class BufferedFile {

    private byte[] out;
    private byte[] in;
    private FileOnDisk file;
    private int outCount = 0;
    private long actualPosition;
    private long actualOutFileSize;
    private long readPosition = -1L;
    private int inCount;
    private long writePosition = -1L;
    private long outFileSize;
    private long position;

    public BufferedFile(FileOnDisk file, int inBufferSize, int outBufferSize) throws IOException {
        this.file = file;
        actualOutFileSize = outFileSize = file.size();
        in = new byte[inBufferSize];
        out = new byte[outBufferSize];
        position = 0L;
    }

    public void write(byte[] array, int offset, int length) throws IOException {
        try {
            if (outFileSize < position + length)
                outFileSize = length + position;

            if (writePosition != -1L && (position < writePosition || outCount + writePosition < position))
                flush();

            if (writePosition != -1L && position + length > out.length + writePosition) {
                int i = (int) (out.length + writePosition - position);
                System.arraycopy(array, offset, out, (int) (position - writePosition), i);
                offset += i;
                length -= i;
                position += i;
                outCount = out.length;
                flush();
            }
            if (length > out.length) {
                if (actualPosition != position) {
                    file.position(position);
                    actualPosition = position;
                }
                file.write(array, offset, length);
                actualPosition += length;
                if (actualOutFileSize < actualPosition)
                    actualOutFileSize = actualPosition;

                long l1 = -1L;
                if (position >= readPosition && position < readPosition + inCount)
                    l1 = position;
                else if (readPosition >= position && readPosition < position + length)
                    l1 = readPosition;

                long l2 = -1L;
                if (length + position > readPosition && inCount + readPosition >= position + length)
                    l2 = length + position;
                else if (inCount + readPosition > position && length + position >= readPosition + inCount)
                    l2 = inCount + readPosition;

                if (l1 > -1L && l2 > l1) {
                    int j = (int) (l2 - l1);
                    System.arraycopy(array, (int) (l1 - position + offset), in, (int) (l1 - readPosition), j);
                }
                position += length;
                return;
            }
            if (length > 0) {
                if (writePosition == -1L)
                    writePosition = position;

                System.arraycopy(array, offset, out, (int) (position - writePosition), length);
                position += length;
                if (position - writePosition > outCount)
                    outCount = (int) (position - writePosition);

                return;
            }
        } finally {
            actualPosition = -1L;
        }
    }

    public void write(byte[] array) throws IOException {
        write(array, 0, array.length);
    }

    public File getFile() {
        return file.file;
    }

    public long size() {
        return outFileSize;
    }

    public void read(byte[] array) throws IOException {
        read(array, 0, array.length);
    }

    public void read(byte[] array, int offset, int length) throws IOException {
        try {
            if (array.length < offset + length)
                throw new ArrayIndexOutOfBoundsException(offset + length - array.length);

            if (writePosition != -1L && writePosition <= position && outCount + writePosition >= position + length) {
                System.arraycopy(out, (int) (position - writePosition), array, offset, length);
                position += length;
                return;
            }
            long l1 = position;
            int i = offset;
            int j = length;
            int k;
            if (position >= readPosition && position < readPosition + inCount) {
                k = (int) (inCount - position + readPosition);
                if (length < k)
                    k = length;

                System.arraycopy(in, (int) (position - readPosition), array, offset, k);
                position += k;
                length -= k;
                offset += k;
            }
            if (length > in.length) {
                file.position(position);
                actualPosition = position;
                while (length > 0) {
                    k = file.read(array, offset, length);
                    if (k == -1)
                        break;

                    offset += k;
                    actualPosition += k;
                    position += k;
                    length -= k;
                }
            } else if (length > 0) {
                fill();
                k = length;
                if (k > inCount)
                    k = inCount;

                System.arraycopy(in, 0, array, offset, k);
                offset += k;
                position += k;
                length -= k;
            }
            if (writePosition != -1L) {
                if (writePosition > position && length > 0) {
                    k = (int) (writePosition - position) + offset;
                    if (k > offset + length)
                        k = offset + length;

                    while (offset < k) {
                        array[offset++] = 0;
                        length--;
                        position += 1L;
                    }
                }
                long l2 = -1L;
                if (l1 > writePosition || writePosition >= l1 + j) {
                    if (l1 >= writePosition && writePosition + outCount > l1)
                        l2 = l1;

                } else
                    l2 = writePosition;

                long l3 = -1L;
                if (l1 >= outCount + writePosition || outCount + writePosition > j + l1) {
                    if (l1 + j > writePosition && l1 + j <= outCount + writePosition)
                        l3 = l1 + j;

                } else
                    l3 = writePosition + outCount;

                if (l2 > -1L && l3 > l2) {
                    int m = (int) (l3 - l2);
                    System.arraycopy(out, (int) (l2 - writePosition), array, i + (int) (l2 - l1), m);
                    if (position < l3) {
                        length = (int) (length - l3 + position);
                        position = l3;
                    }
                }
            }
        } finally {
            actualPosition = -1L;
        }
        if (length > 0)
            throw new EOFException();

    }

    public void position(long position) throws IOException {
        if (position < 0L)
            throw new IOException("Invalid seek to " + position + " in file " + getFile());

        this.position = position;
    }

    public void close() throws IOException {
        try {
            flush();
        } finally {
            file.close();
        }
    }

    public void flush() throws IOException {
        if (outCount == 0)
            return;

        if (writePosition != -1L) {
            if (actualPosition != writePosition) {
                file.position(writePosition);
                actualPosition = writePosition;
            }
            try {
                file.write(out, 0, outCount);
            } finally {
                actualPosition += outCount;
                if (actualOutFileSize < actualPosition)
                    actualOutFileSize = actualPosition;

                long l1 = -1L;
                long l2 = -1L;
                if (writePosition >= readPosition && inCount + readPosition > writePosition)
                    l1 = writePosition;
                else if (writePosition <= readPosition && writePosition + outCount > readPosition)
                    l1 = readPosition;

                if (readPosition < outCount + writePosition && outCount + writePosition <= inCount + readPosition)
                    l2 = writePosition + outCount;
                else if (readPosition + inCount > writePosition && readPosition + inCount <= outCount + writePosition)
                    l2 = inCount + readPosition;

                if (l1 > -1L && l1 < l2) {
                    int i = (int) (l2 - l1);
                    System.arraycopy(out, (int) (l1 - writePosition), in, (int) (l1 - readPosition), i);
                }
                writePosition = -1L;
                outCount = 0;
            }
        }
    }

    private void fill() throws IOException {
        inCount = 0;
        if (actualPosition != position) {
            file.position(position);
            actualPosition = position;
        }
        readPosition = position;
        while (in.length > inCount) {
            int i = in.length - inCount;
            if (i > 200000000)
                i = 200000000;

            int j = file.read(in, inCount, i);
            if (j < 0)
                break;

            inCount += j;
            actualPosition += j;
        }
    }

    protected void finalize() throws Throwable {
        try {
            flush();
        } finally {
            super.finalize();
        }
    }
}
