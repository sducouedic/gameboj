package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * A set of tools for bitstrings manipulations
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 */

public final class Bits {
    
    private static final int[] TAB = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0,
            0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08, 0x88,
            0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58, 0xD8,
            0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4,
            0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
            0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C,
            0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42, 0xC2,
            0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2,
            0x72, 0xF2, 0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA,
            0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06, 0x86,
            0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56, 0xD6,
            0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE, 0x2E, 0xAE,
            0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE,
            0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11, 0x91,
            0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49, 0xC9,
            0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9,
            0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5,
            0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D,
            0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D, 0xDD,
            0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3,
            0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3,
            0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B, 0x9B,
            0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47, 0xC7,
            0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37, 0xB7,
            0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF,
            0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    private Bits() {
    }

    /**
     * returns a value which the only "1" in the binary representation is the
     * bit that corresponds to the index
     * 
     * @param index
     *            integer : the position of the "1"
     * @return integer : the masked value
     * @throws IndexOutOfBoundsException
     *             if the index is negative or indicates a position higher than
     *             the maximum position possible in a Integer
     * 
     */
    public static int mask(int index) {
        Objects.checkIndex(index, Integer.SIZE);
        int a = 0b1;
        return a << index;
    }

    /**
     * tests if the bit at the given index of a value is "1"
     * 
     * @param bits
     *            integer : the value we want to test
     * @param index
     *            integer : the index of the bit in the value
     * @return a boolean : true if there is a 1 at the given index, false if not
     * @throws IndexOutOfBoundsException
     *             if the index is negative or indicates a position higher than
     *             the maximum position possible in a Integer
     */
    public static boolean test(int bits, int index) {

        int maskIndex = mask(index);
        return ((bits & maskIndex) != 0);
    }

    /**
     * tests if a given Bit of a value is "1"
     * 
     * @param bits
     *            integer : the value we want to test
     * @param bit
     *            a Bit : a constant of the enum type Bit
     * @return a boolean : true if there is a 1 at the given Bit, false if not
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * Returns an integer which the binary representation is the same as the
     * given value, except one bit at the given index, which will be set to 1 or
     * 0
     * 
     * @param bits
     *            an int : the value which we want to copy and modify
     * @param index
     *            an int: the position of the bit that will take the new value
     * @param newValue
     *            a boolean : bit will take value "1" if true, "0" if false
     * @return an int : the value with potentially one bit inverted
     * @throws IndexOutOfBoundsException
     *             if the index is negative or indicates a position higher than
     *             the maximum position possible in a Integer
     */
    public static int set(int bits, int index, boolean newValue) {
        int bitsTmp = bits & ~mask(index);
        return bitsTmp | mask(index) * (newValue ? 1 : 0);
    }

    // TODO est-ce que j'ai le droit d'ajouter cette methode?
    /**
     * Returns an integer which the binary representation is the same as the
     * given value, except one bit at the given index, which will be set to 1 or
     * 0
     * 
     * @param bits
     *            an int : the value which we want to copy and modify
     * @param bit
     *            a Bit: the bit int bits that will take the new value
     * @param newValue
     *            a boolean : bit will take value "1" if true, "0" if false
     * @return an int : the value with potentially one bit inverted
     * @throws IndexOutOfBoundsException
     *             if the index is negative or indicates a position higher than
     *             the maximum position possible in a Integer
     */
    public static int set(int bits, Bit bit, boolean newValue) {
        return set(bits, bit.index(), newValue);
    }

    /**
     * Keeps only a given number of the low bits of a given value
     * 
     * @param size
     *            an int : the number of low bits we want to keep
     * @param bits
     *            an int : the original value
     * @return an int : a value which the low bits corresponds to the low bits
     *         of "bits", the rest of the bits are only 0's
     * @throws IllegalArgumentException
     *             if size is negative or strictly higher than the maximum size
     *             for an Integer
     */
    public static int clip(int size, int bits) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.SIZE);

        if (Integer.SIZE == size) {
            return bits;
        } else {
            return (bits & (~(0xFFFFFFFF << size)));
        }
    }

    /**
     * Keeps only a given part of the bits of a given value
     * 
     * @param bits
     *            an int : the original value
     * @param start
     *            an int : the first bit of bits we want to keep
     * @param size
     *            an int : the size of the part of bits we want to keep (the bit
     *            start + size is not included)
     * @return an int : a value which the low bits corresponds to the given part
     *         of bits, the rest of the bits are only 0's
     * @throws IndexOutOfBoundsException
     *             if start and size do not represent a valid range
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        return clip(size, bits >>> start);
    };

    /**
     * Returns a value which a given number of low bits are the same as the
     * original value, except that there was a rotation of a given distance on
     * these bits
     * 
     * @param size
     *            an int : the number of low bits we want to keep from the
     *            original value
     * @param bits
     *            an int : the value which we want to extract the bits
     * @param distance
     *            an int : the distance of the rotation
     * @return an int : a value with the rotated low bits of "bits"
     * @throws IllegalArgumentException
     *             if size is not within 0 (included) and 32 (excluded), or of
     *             the given value is not size bits long
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(
                size > 0 && size <= Integer.SIZE && bits == clip(size, bits));

        distance = Math.floorMod(distance, size);

        return clip(size, (bits << distance | bits >>> size - distance));
    };

    /**
     * Extends the sign of an eight-bits value.
     * 
     * @param b
     *            an int : the value which we want to extend the sign
     * @return an int : the bit of index 7 is copied in the bits from 8 to 31
     * @throws IllegalArgumentException
     *             if the parameter is not a valid 8 bits value
     */
    public static int signExtend8(int b) {
        Preconditions.checkBits8(b);
        return (int) ((byte) b);
    };

    /**
     * Reverses the lowest bits with the highest bits of a given value
     * 
     * @param b
     *            an int : the value whose bits we want to reverse
     * @return an int : the bits of index 7 and 0 have been exchanged, same for
     *         1 and 6, 2 and 5, 3 and 4
     * @throws IllegalArgumentException
     *             if the parameter is not a valid 8 bits value
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return TAB[b];
    };

    /**
     * Returns the complements of the original value : every 1 bit becomes 0 and
     * every 0 becomes 1
     * 
     * @param b
     *            an int : the original value
     * @return an int : the complement of b
     * @throws IllegalArgumentException
     *             if the parameter is not a valid 8 bits value
     */
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        return b ^ 0b11111111;
    };

    /**
     * Uses highB and lowB to make a 16-bits integer
     * 
     * @param highB
     *            an int : the 8 lowest bits of highB will become the high bits
     *            of the result
     * @param lowB
     *            an int : the 8 lowest bits of lowB will become the low bits of
     *            the result
     * @return an int : a kind of concatenation of highB and lowB
     * @throws IllegalArgumentException
     *             if a parameter is not a valid 8-bits value
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(highB);
        Preconditions.checkBits8(lowB);
        return (highB << Byte.SIZE | lowB);
    };
}
