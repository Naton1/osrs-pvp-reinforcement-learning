package com.elvarg.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The {@link ThreadLocalRandom} wrapper that provides additional functionality
 * for generating pseudo-random numbers. In order to avoid sharing instances of
 * this class across multiple threads, this should only be instantiated locally
 * unless certain that it will never be accessed by another thread.
 *
 * @author lare96 <http://github.com/lare96>
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 */
public final class RandomGen {

    /**
     * The backing {@link ThreadLocalRandom} that will pseudorandomly generate
     * numbers. It is generally preferred to use this over {@link Random}
     * because although {@code Random} is thread safe; the same seed is shared
     * concurrently, which leads to contention between multiple threads and
     * overhead as a result of that. Surprisingly because of the way that
     * {@code ThreadLocalRandom} works, even in completely single-threaded
     * situations it runs up to three times faster than {@code Random}.
     *
     * @see <a
     * href="http://java-performance.info/java-util-random-java-util-concurrent-threadlocalrandom-multithreaded-environments/">java.util.Random
     * and java.util.concurrent.ThreadLocalRandom in multithreaded
     * environments</a>
     */
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * Gets the backing {@link ThreadLocalRandom}.
     *
     * @return the backing random instance.
     */
    public ThreadLocalRandom get() {
        return random;
    }

    /**
     * Returns a pseudo-random {@code int} value between inclusive {@code min}
     * and inclusive {@code max}.
     *
     * @param min the minimum inclusive number.
     * @param max the maximum inclusive number.
     * @return the pseudo-random {@code int}.
     * @throws IllegalArgumentException if {@code max - min + 1} is less than {@code 0}.
     * @see {@link #exclusive(int)}.
     */
    public int inclusive(int min, int max) {
        if (max < min) {
            max = min + 1;
        }
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Returns a pseudo-random {@code int} value between inclusive {@code 0} and
     * inclusive {@code range}.
     *
     * @param range the maximum inclusive number.
     * @return the pseudo-random {@code int}.
     * @throws IllegalArgumentException if {@code max - min + 1} is less than {@code 0}.
     * @see {@link #exclusive(int)}.
     */
    public int inclusive(int range) {
        return inclusive(0, range);
    }

    /**
     * Returns a pseudo-random {@code int} value between inclusive {@code min}
     * and inclusive {@code max} excluding the specified numbers within the
     * {@code excludes} array.
     *
     * @param min the minimum inclusive number.
     * @param max the maximum inclusive number.
     * @return the pseudo-random {@code int}.
     * @throws IllegalArgumentException if {@code max - min + 1} is less than {@code 0}.
     * @see {@link #inclusive(int, int)}.
     */
    public int inclusiveExcludes(int min, int max, int... exclude) {
        Arrays.sort(exclude);

        int result = inclusive(min, max);
        while (Arrays.binarySearch(exclude, result) >= 0) {
            result = inclusive(min, max);
        }

        return result;
    }

    /**
     * Returns a pseudo-random {@code float} between inclusive {@code 0} and
     * exclusive {@code range}.
     *
     * @param range The exclusive range.
     * @return The pseudo-random {@code float}.
     * @throws IllegalArgumentException If the specified range is less than {@code 0}.
     */
    public float floatRandom(float range) {
        if (range < 0F)
            throw new IllegalArgumentException("range <= 0");
        return random.nextFloat() * range;
    }

    /**
     * Pseudo-randomly retrieves an index from {@code array}.
     *
     * @param array the array to retrieve an index from.
     * @return the element retrieved from the array.
     */
    public int randomIndex(Object[] array) {
        return (int) (random.nextDouble() * array.length);
    }

    /**
     * Pseudo-randomly retrieves a element from {@code array}.
     *
     * @param array the array to retrieve an element from.
     * @return the element retrieved from the array.
     */
    public <T> T random(T[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code int} from this {@code array}.
     *
     * @param array the array to retrieve an {@code int} from.
     * @return the {@code int} retrieved from the array.
     */
    public int random(int[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code long} from this {@code array}.
     *
     * @param array the array to retrieve an {@code long} from.
     * @return the {@code long} retrieved from the array.
     */
    public long random(long[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code double} from this {@code array}.
     *
     * @param array the array to retrieve an {@code double} from.
     * @return the {@code double} retrieved from the array.
     */
    public double random(double[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code short} from this {@code array}.
     *
     * @param array the array to retrieve an {@code short} from.
     * @return the {@code short} retrieved from the array.
     */
    public short random(short[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code byte} from this {@code array}.
     *
     * @param array the array to retrieve an {@code byte} from.
     * @return the {@code byte} retrieved from the array.
     */
    public byte random(byte[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code float} from this {@code array}.
     *
     * @param array the array to retrieve an {@code float} from.
     * @return the {@code float} retrieved from the array.
     */
    public float random(float[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code boolean} from this {@code array}.
     *
     * @param array the array to retrieve an {@code boolean} from.
     * @return the {@code boolean} retrieved from the array.
     */
    public boolean random(boolean[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves an {@code char} from this {@code array}.
     *
     * @param array the array to retrieve an {@code char} from.
     * @return the {@code char} retrieved from the array.
     */
    public char random(char[] array) {
        return array[(int) (random.nextDouble() * array.length)];
    }

    /**
     * Pseudo-randomly retrieves a element from {@code list}.
     *
     * @param list the list to retrieve an element from.
     * @return the element retrieved from the list.
     */
    public <T> T random(List<T> list) {
        return list.get((int) (random.nextDouble() * list.size()));
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code T} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public <T> T[] shuffle(T[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            T a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code int} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public int[] shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code long} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public long[] shuffle(long[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            long a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code double} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public double[] shuffle(double[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            double a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code short} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public short[] shuffle(short[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            short a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code byte} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public byte[] shuffle(byte[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            byte a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code float} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public float[] shuffle(float[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            float a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code boolean} array.
     *
     * @param array the array that will be shuffled.
     * @return the shuffled array.
     */
    public boolean[] shuffle(boolean[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            boolean a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * An implementation of the Fisher-Yates shuffle algorithm that will shuffle
     * the elements of an {@code char} array.
     *
     * @param array the array that will be shuffled.
     */
    public char[] shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * Determines if a pseudorandomly generated double rounded to two decimal
     * places is below or equal to {@code value}.
     *
     * @param value the value to determine this for.
     * @return {@code true} if successful, {@code false} otherwise.
     */
    public boolean success(double value) {
        return random.nextDouble() <= value;
    }
}