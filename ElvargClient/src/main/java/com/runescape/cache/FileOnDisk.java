package com.runescape.cache;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class FileOnDisk {
    public final long limit;
    public final File file;
    private long position;
    private RandomAccessFile stream;

    public FileOnDisk(File f, String s, long l) throws IOException {
        file = f;
        if (l < 0L)
            l = Long.MAX_VALUE;

        if (s == null) {
            System.out.println("NULLL");
        }

        limit = l;
        if (!s.equals("r") && f.exists() && (!f.isFile() || f.length() > l))
            f.delete();

        RandomAccessFile r = stream = new RandomAccessFile(f, s);
        try {
            int n = r.read();
            if (n > -1 && !s.equals("r")) {
                r.seek(0L);
                r.write(n);
            }
            r.seek(0L);
            r = null;
        } finally {
            if (r != null)
                r.close();
        }
    }

    public void write(int n) throws IOException {
        if (position >= limit) {
            if (position == limit) {
                stream.write(1);
                ++position;
            }
            throw new IOException();
        }
        stream.write(n);
        ++position;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int n, int n1) throws IOException {
        if (position + (long) n1 > limit) {
            position(limit);
            stream.write(1);
            ++position;
            throw new IOException();
        }
        stream.write(b, n, n1);
        position += (long) n1;
    }

    public int read() throws IOException {
        int n = stream.read();
        if (n > -1)
            ++position;

        return n;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int n, int n1) throws IOException {
        n = stream.read(b, n, n1);
        if (n > 0)
            position += (long) n;

        return n;
    }

    public long size() throws IOException {
        return stream.length();
    }

    public void position(long l) throws IOException {
        if (l < 0L || l > limit)
            throw new IOException();

        if (position == l)
            return;

        stream.seek(l);
        position = l;
    }

    public long position() {
        return position;
    }

    public void close() {
        RandomAccessFile r = stream;
        if (r == null)
            return;

        stream = null;
        try {
            r.close();
        } catch (Exception x) {
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
