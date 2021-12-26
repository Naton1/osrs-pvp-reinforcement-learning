package com.runescape.cache;

import java.io.IOException;

public final class JFS32 {
    public final BufferedFile file;
    public final int blockSize;
    public final long blockSize0;
    public final long blockLimit;
    private final byte[] buffer = new byte[8];

    public JFS32(BufferedFile file, int blockSize, int blockLimit) {
        if (file == null || blockSize == 0) {
            throw new IllegalArgumentException();
        }
        this.file = file;
        this.blockSize = blockSize;
        this.blockSize0 = ((long) blockSize & 0xffffffffL) + 4L;
        this.blockLimit = (long) blockLimit & 0xffffffffL;
    }

    private static void putInt(byte[] array, int offset, int v) {
        array[offset] = (byte) (v >>> 24);
        array[offset + 1] = (byte) (v >>> 16);
        array[offset + 2] = (byte) (v >>> 8);
        array[offset + 3] = (byte) v;
    }

    private static long getUInt(byte[] array, int offset) {
        return ((long) (array[offset] & 0xff) << 24L) | (long) (((array[offset + 1] & 0xff) << 16) | ((array[offset + 2] & 0xff) << 8) | (array[offset + 3] & 0xff));
    }

    public int getPointers(int ptr) {
        if (ptr == 0) {
            return 0;
        }
        return getPointers((long) ptr & 0xffffffffL, -1L, null, 0, 0);
    }

    public int getPointers(int ptr, int[] out) {
        if (ptr == 0) {
            return 0;
        }
        return getPointers((long) ptr & 0xffffffffL, -1L, out, 0, out.length);
    }

    public int getPointers(int ptr, int[] out, int offset, int length) {
        if (ptr == 0) {
            return 0;
        }
        if (out.length < offset + length || offset < 0 || length < 0 || offset + length < 0) {
            throw new IndexOutOfBoundsException();
        }
        return getPointers((long) ptr & 0xffffffffL, -1L, out, offset, length);
    }

    private int getPointers(long ptr, long size, int[] out, int offset, int length) {
        if (size == -1L) {
            size = file.size();
        }
        if (size < 8L + blockSize0) {
            return 0;
        }
        long pos;
        int count = 0;
        while ((pos = 4L + ptr * blockSize0) + 4L <= size) {
            if (out != null && count < length) {
                out[offset + count] = (int) ptr;
            }
            ++count;
            try {
                file.position(pos);
                file.read(buffer, 0, 4);
            } catch (IOException ex) {
                break;
            }
            long next = getUInt(buffer, 0);
            if (next == 0L || next == ptr) {
                break;
            }
            ptr = next;
        }
        return count;
    }

    public int getBlockCount() {
        long size = file.size();
        long count = (size - 8L) / blockSize0;
        if (count > blockLimit) {
            count = blockLimit;
        }
        return (int) count;
    }

    public int getFree() {
        return getFree0(null, 0, 0);
    }

    public int getFree(int[] out) {
        return getFree(out, 0, out.length);
    }

    public int getFree(int[] out, int offset, int length) {
        if (out.length < offset + length || offset < 0 || length < 0 || offset + length < 0) {
            throw new IndexOutOfBoundsException();
        }
        return getFree0(out, offset, length);
    }

    public int getFree0(int[] out, int offset, int length) {
        long size = file.size();
        if (size < 8L + blockSize0) {
            return 0;
        }
        try {
            file.position(0L);
            file.read(buffer, 0, 4);
        } catch (IOException ex) {
            return 0;
        }
        long ptr = getUInt(buffer, 0);
        if (ptr == 0L) {
            return 0;
        }
        return getPointers(ptr, size, out, offset, length);
    }

    public int getTotalSize(int blocks) {
        return blocks * blockSize;
    }

    public boolean validPointer(int ptr) {
        return ptr != 0 && file.size() >= 8L + blockSize0 * ((long) ptr & 0xffffffffL);
    }

    public int getRequiredBlocks(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        int count = (size + blockSize - 1) / blockSize;
        if (count == 0) {
            count = 1;
        }
        return count;
    }

    public int alloc(int count) {
        return alloc(count, null, 0);
    }

    public int alloc(int count, int[] out) {
        return alloc(count, out, 0);
    }

    public int alloc(int count, int[] out, int outOffset) {
        if (count == 0 || ((long) count & 0xffffffffL) > blockLimit) {
            return 0;
        }
        return alloc0(0L, count, out, outOffset);
    }

    public int realloc(int ptr, int count) {
        return realloc(ptr, count, null, 0);
    }

    public int realloc(int ptr, int count, int[] out) {
        return realloc(ptr, count, out, 0);
    }

    public int realloc(int ptr, int count, int[] out, int outOffset) {
        if (count != 0 && ((long) count & 0xffffffffL) > blockLimit) {
            count = 0;
        }
        return alloc0((long) ptr & 0xffffffffL, count, out, outOffset);
    }

    private int alloc0(long ptr, int count, int[] out, int outOffset) {
        if (out != null && (outOffset < 0 || (long) outOffset + ((long) count & 0xffffffffL) > (long) out.length)) {
            throw new IndexOutOfBoundsException();
        }
        long start = 0L;
        boolean realloc = true;
        long size = file.size();
        if (size >= 8L + blockSize0) {
            try {
                file.position(0L);
                file.read(buffer, 0, 4);
            } catch (IOException ex) {
            }
            start = getUInt(buffer, 0);
            if (start == ptr) {
                ptr = 0L;
            }
        }
        if (ptr == 0L) {
            ptr = start;
            realloc = false;
        }
        int first = 0;
        long prev = 0L;
        if (ptr != 0L) {
            while (true) {
                long pos;
                if (ptr == 0L || ptr > blockLimit || (pos = 4L + ptr * blockSize0) + 4L > size) {
                    if (realloc && start != 0L) {
                        realloc = false;
                        ptr = start;
                        continue;
                    }
                    ptr = 0L;
                    break;
                }
                if (count == 0) {
                    break;
                }
                try {
                    file.position(pos);
                    file.read(buffer, 0, 4);
                } catch (IOException ex) {
                    ptr = 0L;
                    break;
                }
                long next = getUInt(buffer, 0);
                if (next == ptr) {
                    next = 0L;
                }
                int ptr_ = (int) ptr;
                if (out != null) {
                    out[outOffset++] = ptr_;
                }
                if (prev != 0L) {
                    putInt(buffer, 0, ptr_);
                    try {
                        file.position(prev);
                        file.write(buffer, 0, 4);
                    } catch (IOException ex) {
                    }
                } else {
                    first = ptr_;
                }
                prev = pos;
                ptr = next;
                --count;
            }
            if (!realloc) {
                putInt(buffer, 0, (int) ptr);
                if (ptr == 0L) {
                    putInt(buffer, 4, 0);
                    try {
                        file.position(0L);
                        file.write(buffer, 0, 8);
                    } catch (IOException ex) {
                    }
                } else {
                    try {
                        file.position(0L);
                        file.write(buffer, 0, 4);
                    } catch (IOException ex) {
                    }
                }
            } else if (ptr != 0L) {
                free0(ptr, null, 0, 0, size);
            }
        }
        if (count != 0) {
            ptr = (size - 8L) / blockSize0;
            if (ptr + ((long) count & 0xffffffffL) > blockLimit) {
                if (first != 0) {
                    free1((long) first & 0xffffffffL, (prev - 4L) / blockSize0, size);
                }
                return 0;
            }
            for (; count != 0; --count) {
                long pos = 4L + ++ptr * blockSize0;
                int ptr_ = (int) ptr;
                if (out != null) {
                    out[outOffset++] = ptr_;
                }
                if (prev != 0L) {
                    putInt(buffer, 0, ptr_);
                    try {
                        file.position(prev);
                        file.write(buffer, 0, 4);
                    } catch (IOException ex) {
                        if (first != 0) {
                            free1((long) first & 0xffffffffL, (prev - 4L) / blockSize0, size);
                        }
                        return 0;
                    }
                } else {
                    first = ptr_;
                }
                prev = pos;
            }
        }
        putInt(buffer, 0, 0);
        try {
            file.position(prev);
            file.write(buffer, 0, 4);
        } catch (IOException ex) {
            if (first != 0) {
                free1((long) first & 0xffffffffL, (prev - 4L) / blockSize0, size);
            }
            return 0;
        }
        return first;
    }

    public int free(int ptr) {
        if (ptr == 0) {
            return 0;
        }
        return free0(ptr, null, 0, 0);
    }

    public int free(int ptr, int[] out) {
        if (ptr == 0) {
            return 0;
        }
        return free0(ptr, out, 0, out.length);
    }

    public int free(int ptr, int[] out, int offset, int length) {
        if (ptr == 0) {
            return 0;
        }
        if (out.length < offset + length || offset < 0 || length < 0 || offset + length < 0) {
            throw new IndexOutOfBoundsException();
        }
        return free0(ptr, out, offset, length);
    }

    private int free0(int ptr, int[] out, int offset, int length) {
        long start = (long) ptr & 0xffffffffL;
        long size;
        if (start <= blockLimit && (size = file.size()) >= 8L + start * blockSize0) {
            return free0(start, out, offset, length, size);
        }
        return 0;
    }

    public void free(int start_, int end_) {
        if (start_ == 0) {
            return;
        }
        long size;
        long start = (long) start_ & 0xffffffffL;
        if (start > blockLimit || (size = file.size()) < 8L + start * blockSize0) {
            return;
        }
        long end;
        if (end_ == 0 || (end = ((long) end_ & 0xffffffffL)) > blockLimit || 8L + end * blockSize0 > size) {
            free0(start, null, 0, 0, size);
        } else {
            free1(start, end, size);
        }
    }

    private int free0(long start, int[] out, int offset, int length, long size) {
        int count = 0;
        long end = start;
        while (true) {
            if (out != null && count < length) {
                out[offset + count] = (int) end;
            }
            ++count;
            try {
                file.position(4L + end * blockSize0);
                file.read(buffer, 0, 4);
            } catch (IOException ex) {
                break;
            }
            long ptr = getUInt(buffer, 0);
            if (ptr == 0L || ptr > blockLimit || ptr == start || ptr == end || 8L + ptr * blockSize0 > size) {
                break;
            }
            end = ptr;
        }
        free1(start, end, size);
        return count;
    }

    private void free1(long start, long end, long size) {
        if (size >= 8L) {
            long first = 0L;
            long last = 0L;
            try {
                file.position(0L);
                file.read(buffer, 0, 8);
                first = getUInt(buffer, 0);
                last = getUInt(buffer, 4);
            } catch (IOException ex) {
            }
            if (first != 0L && 8L + first * blockSize0 <= size && first <= blockLimit) {
                if (start == first || end == first) {
                    return;
                }
                if (last == 0L || last > blockLimit || 8L + last * blockSize0 > size) {
                    last = first;
                    long prev = 0L;
                    while (true) {
                        try {
                            file.position(4L + last * blockSize0);
                            file.read(buffer, 0, 4);
                        } catch (IOException ex) {
                            last = prev;
                            break;
                        }
                        long ptr = getUInt(buffer, 0);
                        if (ptr == 0L || ptr > blockLimit || ptr == first || ptr == last || ptr == start || ptr == end || 8L + ptr * blockSize0 > size) {
                            break;
                        }
                        prev = last;
                        last = ptr;
                    }
                    if (last == 0L) {
                        first = 0L;
                    }
                    if (start == last || end == last) {
                        putInt(buffer, 0, (int) last);
                        try {
                            file.position(4L);
                            file.write(buffer, 0, 4);
                        } catch (IOException ex) {
                        }
                        return;
                    }
                } else if (start == last || end == last) {
                    return;
                }
                if (first != 0L) {
                    putInt(buffer, 0, (int) start);
                    try {
                        file.position(4L + last * blockSize0);
                        file.write(buffer, 0, 4);
                    } catch (IOException ex) {
                    }
                    putInt(buffer, 0, (int) end);
                    try {
                        file.position(4L);
                        file.write(buffer, 0, 4);
                    } catch (IOException ex) {
                    }
                    return;
                }
            }
        }
        putInt(buffer, 0, (int) start);
        putInt(buffer, 4, (int) end);
        try {
            file.position(0L);
            file.write(buffer, 0, 8);
        } catch (IOException ex) {
        }
    }

    public int read(int ptr, int start, byte[] array) {
        return readOrWrite(ptr, start, array, 0, array.length, false);
    }

    public int read(int ptr, int start, byte[] array, int offset, int length) {
        return readOrWrite(ptr, start, array, offset, length, false);
    }

    public int write(int ptr, int start, byte[] array) {
        return readOrWrite(ptr, start, array, 0, array.length, true);
    }

    public int write(int ptr, int start, byte[] array, int offset, int length) {
        return readOrWrite(ptr, start, array, offset, length, true);
    }

    private int readOrWrite(int ptr_, int start, byte[] array, int offset, int length, boolean write) {
        if (ptr_ == 0 || length == 0) {
            return 0;
        }
        if (start < 0 || array.length < offset + length || offset < 0 || length < 0 || offset + length < 0) {
            throw new IndexOutOfBoundsException();
        }
        long ptr = (long) ptr_ & 0xffffffffL;
        if (ptr > blockLimit) {
            return 0;
        }
        long size = file.size();
        int length_ = length;
        while (ptr != 0L && ptr < blockLimit) {
            long pos = 4L + ptr * blockSize0;
            if (pos + 4L > size) {
                break;
            }
            int begin = 0;
            if (start > 0) {
                begin += start;
                start -= blockSize;
            }
            if (begin < blockSize) {
                int count = blockSize - begin;
                try {
                    file.position(pos - (long) count);
                    if (count > length) {
                        count = length;
                    }
                    if (!write) {
                        file.read(array, offset, count);
                    } else {
                        file.write(array, offset, count);
                    }
                } catch (IOException ex) {
                    break;
                }
                offset += count;
                length -= count;
                if (length == 0) {
                    break;
                }
            }
            try {
                file.position(pos);
                file.read(buffer, 0, 4);
            } catch (IOException ex) {
                break;
            }
            ptr = getUInt(buffer, 0);
        }
        return length_ - length;
    }
}
