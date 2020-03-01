package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import javax.swing.text.ChangedCharSetException;

import ch.epfl.gameboj.Preconditions;

/**
 * A set of tools for bit vectors manipulations
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class BitVector {

    public static final int ALL_ZEROS_INTEGER = 0b00000000_00000000_00000000_00000000;
    public static final int ALL_ONES_INTEGER = 0b11111111_11111111_11111111_11111111;

    private final int[] vector;

    private enum Extraction {
        WRAPPED, ZERO_EXTENDED
    };

    /**
     * Constructs and returns a new BitVector, filled with only zeros or only
     * ones
     * 
     * @param size
     *            : an integer, the size (in bits) of the vector
     * @param initialValue
     *            : a boolean the value with which we will fill the vector
     *            (false for zero and true for one)
     * @throws IllegalArgumentException
     *             : if the size is not strictly positive or if it is not a
     *             multiple of 32
     */
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size > 0 && is32Multiple(size));

        int numberOfInts = size / Integer.SIZE;
        vector = new int[numberOfInts];
        if (initialValue) {
            Arrays.fill(vector, ALL_ONES_INTEGER);
        }
    }

    /**
     * Constructs and returns a new BitVector, containing only zeros
     * 
     * @param size
     *            : an integer : the size (in bits) of the vector
     * 
     * @throws IllegalArgumentException
     *             : if the size is not strictly positive or if it is not a
     *             multiple of 32
     */
    public BitVector(int size) {
        this(size, false);
    }

    // for internal use only
    private BitVector(int[] elements) {
        vector = elements;
    }

    /**
     * Helps build a BitVector from scratch
     * 
     * @author Sophie du Couédic (260007)
     * @author Arnaud Robert (287964)
     */
    public static final class Builder {

        private int[] bits;

        /**
         * Create and return a new Builder able to build a BitVector of the
         * given size
         * 
         * @param size
         *            : an integer the size of the future BitVector
         * @throws TODO
         */
        public Builder(int size) {
            Preconditions.checkArgument(size > 0 && is32Multiple(size));
            bits = new int[size / Integer.SIZE];
        }

        /**
         * defines the value of the given index's byte
         * 
         * @param index
         *            : an integer, the index of the byte that we want to set
         * @param value
         *            : an integer, the 8-bits value we want to place in the
         *            vector at the given index
         * @return Builder : the builder itself for further settings
         * @throws IllegalStateException
         *             : if the builder has been already used to build some
         *             BitVector
         * @throws IndexOutOfBoundsException
         *             : if the index is less than zero or if it is greater than
         *             the size of the future BitVector, divided by 8
         * @throws IllegalArgumentException
         *             : if the value is not an 8-bits value
         */
        public Builder setByte(int index, int value) {
            checkIfBuiltAlready();

            int ratioIntByte = Integer.SIZE / Byte.SIZE;

            Objects.checkIndex(index, bits.length * ratioIntByte);
            Preconditions.checkBits8(value);

            int indexInBits = index / ratioIntByte;
            int subIndexInBits = index % ratioIntByte;

            int mask = 0b11111111;
            int temp1 = bits[indexInBits]
                    & ~(mask << (subIndexInBits * Byte.SIZE));
            int temp2 = value << (subIndexInBits * Byte.SIZE);

            bits[indexInBits] = temp1 | temp2;

            return this;
        }

        /**
         * Creates a new BitVector
         * 
         * @return the newly created BitVector, not null
         */
        public BitVector build() {
            checkIfBuiltAlready();
            BitVector result = new BitVector(bits);
            bits = null;
            return result;
        }

        private void checkIfBuiltAlready() {
            if (bits == null) {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * gives the number of bits in the vector
     * 
     * @return an integer : the number of bits in the vectors
     */
    public int size() {
        return vector.length * Integer.SIZE;
    }

    /**
     * gives the value of the bit in the vector at the given index
     * 
     * @param index
     *            : an integer the index of the bit we want to test
     * @return a boolean : true if the bit is one, false if it is zero
     * @throws IndexOutOfBoundsException
     *             : if the index is less than zero or bigger than the size of
     *             the vector
     */
    public boolean testBit(int index) {
        Objects.checkIndex(index, this.size());
        return Bits.test(vector[index / Integer.SIZE], index % Integer.SIZE);
    }

    /**
     * Computes the one's-complement of the BitVector
     * 
     * @return a BitVector : the one's-complement of the current BitVector
     */
    public BitVector not() {
        int[] elements = new int[vector.length];
        int i = 0;
        for (int a : vector) {
            elements[i] = ~a;
            i++;
        }
        return new BitVector(elements);
    }

    /**
     * Computes the bit-by-bit logical conjunction "and" of the current
     * BitVector with another BitVector
     * 
     * @param that
     *            : a BitVector, the BitVector with which we want to compute the
     *            logical conjunction
     * @return a BitVector : the result of the bit-by-bit conjunction "and" of
     *         the current BitVector and that
     */
    public BitVector and(BitVector that) {
        return andOr(that, true);
    }

    /**
     * Computes the bit-by-bit logical disjunction "or" of the current BitVector
     * with another BitVector
     * 
     * @param that
     *            : a BitVector, the BitVector with which we want to compute the
     *            logical disjunction
     * @return the result of the bit-by-bit disjunction "or" of the current
     *         BitVector and that
     */
    public BitVector or(BitVector that) {
        return andOr(that, false);
    }

    /**
     * Computes the bit-by-bit logical "xor" of the current BitVector with
     * another BitVector
     * 
     * @param that
     *            : the BitVector with which we want to compute the logical xor
     * @return the result of the bit-by-bit disjunction "or" of the current
     *         BitVector and that
     */
    public BitVector xor(BitVector that) {
        Preconditions.checkArgument(that.size() == size());
        int length = vector.length;

        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            int other = that.vector[i];
            int me = vector[i];
            result[i] = other ^ me;
        }

        return new BitVector(result);
    }

    /**
     * Extracts a BitVector of the given length, from the infinite extension by
     * zero of the current BitVector, from given index
     * 
     * @param index
     *            : an integer, the index from which we extract the new
     *            BitVector in the infinite vector
     * @param length
     *            : an integer, the length of the BitVector we want to extract
     * @return a BitVector : new the BitVector extracted from the infinite
     *         extension by zero of the current BitVector
     * @throws IllegalArgumentException
     *             : if the length is less or equal to zero, or if it is not a
     *             multiple of 32
     */
    public BitVector extractZeroExtended(int index, int length) {
        return new BitVector(extract(index, length, Extraction.ZERO_EXTENDED));
    }

    /**
     * Extracts a BitVector of the given length, from the infinite wrapped
     * extension of the current BiVector, from the given index
     * 
     * @param index
     *            : an integer, the index from which we extract the new
     *            BitVector in the infinite vector
     * @param length
     *            : an integer, the length of the BitVector we want to extract
     * @return a BitVector : the new BitVector extracted from the infinite
     *         wrapped extension of the current BitVector
     * @throws IllegalArgumentException
     *             : if the length is less or equal to zero, or if it is not a
     *             multiple of 32
     */
    public BitVector extractWrapped(int index, int length) {
        return new BitVector(extract(index, length, Extraction.WRAPPED));
    }

    /**
     * Shifts the BitVector the given distance (to the left if the distance is
     * positive, right if the distance is negative), by adding zeros at the
     * opposite extremity
     * 
     * @param distance
     *            : an integer, the distance we want to do the shifting
     * @return a BitVector : the new BitVector that is the shifted current
     *         vector
     */
    public BitVector shift(int distance) {
        return new BitVector(
                extract(-distance, size(), Extraction.ZERO_EXTENDED));
    }

    /**
     * non java-doc
     * 
     * @see java.lang.Object
     */
    @Override
    public boolean equals(Object that) {
        Preconditions.checkArgument(that instanceof BitVector);
        if (this.size() != ((BitVector) that).size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (this.testBit(i) != (((BitVector) that).testBit(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * non java-doc
     * 
     * @see java.lang.Object
     */
    @Override
    public int hashCode() {
        return vector.hashCode();
    }

    /**
     * non java-doc
     * 
     * @see java.lang.Object
     */
    @Override
    public String toString() {
        String binary = "";
        for (int i = this.size() - 1; i >= 0; i--) {
            binary += this.testBit(i) ? "1" : "0";
        }
        return binary;
    }

    private static boolean is32Multiple(int a) {
        return a % 32 == 0;
    }

    private int computeInt(Extraction type, int index) {
        int size = vector.length;
        if (size <= index || index < 0) {
            return type == Extraction.ZERO_EXTENDED ? ALL_ZEROS_INTEGER
                    : vector[Math.floorMod(index, size)];
        } else {
            return vector[Math.floorMod(index, size)];
        }

    }

    private BitVector andOr(BitVector that, boolean and) {
        Objects.requireNonNull(that);
        Preconditions.checkArgument(that.size() == size());
        int length = vector.length;

        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            int other = that.vector[i];
            int me = vector[i];
            result[i] = and ? other & me : other | me;
        }

        return new BitVector(result);
    }

    private int[] extract(int index, int length, Extraction type) {

        Preconditions.checkArgument(is32Multiple(length) && length != 0);

        int arrayIndex = Math.floorDiv(index, Integer.SIZE);
        int intIndex = Math.floorMod(index, Integer.SIZE);

        int nbIntegersToCompute = Math.floorDiv(length, Integer.SIZE);
        int[] array = new int[nbIntegersToCompute];

        int temp1 = computeInt(type, arrayIndex);
        int temp2 = 0;
        for (int i = 0; i < nbIntegersToCompute; i++) {
            if (is32Multiple(index)) {
                array[i] = computeInt(type, i + arrayIndex);
            } else {
                temp2 = computeInt(type, i + arrayIndex + 1);
                temp1 = temp1 >>> intIndex;
                array[i] = (temp2 << (32 - intIndex)) | temp1;

                temp1 = temp2;
            }
        }

        return array;
    }
}
