package com.example.test;

import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import lombok.Data;
import org.apache.commons.lang3.RandomUtils;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * This class supplies pre-generated random numbers to be used in places where the calls to Random.nextXXX takes an
 * expressive time from the thing you are actually trying to measure.
 */
@Data
public class FastRand {

    public enum GenerationMethod {
        /**
         * Use java default Random generation, call it at each request for a number.
         */
        APPROACH_1,
        /**
         * Use java default Random generation. Generate a pool of random numbers, iterate through it when a number is
         * requested. When the end of the pool is reached, generate a random number and use it as an index on the pool,
         * where which the pool will restart being read from.
         */
        APPROACH_2,
        /**
         * Use the class it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom from library it.unimi.dsi:dsiutils. Call it at
         * each request for a number.
         */
        APPROACH_3,
    }

    static class GenerationMethodFactory {
        public IGenerationMethod of(GenerationMethod generationMethod) {
            return switch (generationMethod) {
                case APPROACH_1 -> new Approach1GenerationMethod();
                case APPROACH_2 -> new Approach2GenerationMethod();
                case APPROACH_3 -> new Approach3GenerationMethod();
            };
        }
    }

    public interface IGenerationMethod {
        void init(Object... params);

        int nextInt(int s, int e);

        long nextLong(long s, long e);
    }

    static class Approach1GenerationMethod implements IGenerationMethod {
        Random r = new Random(System.currentTimeMillis());

        @Override
        public void init(Object... params) {

        }

        @Override
        public int nextInt(int s, int e) {
            return r.nextInt(s, e);
        }

        @Override
        public long nextLong(long s, long e) {
            return r.nextLong(s, e);
        }
    }

    static class Approach2GenerationMethod implements IGenerationMethod {
        Random r = new Random(System.currentTimeMillis());
        int[] ints;
        int i;
        long[] longs;
        int j;
        int size;

        @Override
        public void init(Object... params) {
            size = (Integer) params[0];
            this.ints = IntStream.range(0, size)
                    .map(i2 -> RandomUtils.nextInt((Integer) params[1], (Integer) params[2]))
                    .toArray();

            longs = IntStream.range(0, size)
                    .mapToLong(i2 -> RandomUtils.nextLong((Long) params[3], (Long) params[4]))
                    .toArray();
        }

        @Override
        public int nextInt(int s, int e) {
            if (size == 0)
                throw new IllegalStateException("Uninitialized");
            if (i >= ints.length)
                i = r.nextInt(size - 1);

            return ints[i++];
        }

        @Override
        public long nextLong(long s, long e) {
            if (size == 0)
                throw new IllegalStateException("Uninitialized");
            if (j >= longs.length)
                j = r.nextInt(size - 1);

            return longs[j++];
        }
    }

    static class Approach3GenerationMethod implements IGenerationMethod {
        XoRoShiRo128PlusPlusRandom otherRandom = new XoRoShiRo128PlusPlusRandom(System.currentTimeMillis());

        @Override
        public void init(Object... params) {
        }

        @Override
        public int nextInt(int s, int e) {
            return otherRandom.nextInt(s, e);
        }

        @Override
        public long nextLong(long s, long e) {
            return otherRandom.nextLong(s, e);
        }
    }

    IGenerationMethod generationMethodImpl;
    int intsRangeStart;
    int intsRangeEnd;
    long longsRangeStart;
    long longsRangeEnd;

    public FastRand(GenerationMethod generationMethod, int intsRangeStart, int intsRangeEnd,
            long longsRangeStart, long longsRangeEnd) {
        this.intsRangeStart = intsRangeStart;
        this.intsRangeEnd = intsRangeEnd;
        this.longsRangeStart = longsRangeStart;
        this.longsRangeEnd = longsRangeEnd;

        final var generationMethodFactory = new GenerationMethodFactory();
        generationMethodImpl = generationMethodFactory.of(generationMethod);
    }

    public int nextInt() {
        return generationMethodImpl.nextInt(intsRangeStart, intsRangeEnd);
    }

    public long nextLong() {
        return generationMethodImpl.nextLong(longsRangeStart, longsRangeEnd);
    }

    @SuppressWarnings("unused")
    public void init(Object... params) {
        generationMethodImpl.init(params);
    }
}
