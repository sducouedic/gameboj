package ch.epfl.gameboj.bits;

/**
 * A single bit
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 */

public interface Bit {

    /**
     * Returns the position of the Bit, in the order of its enum declaration
     * 
     * Is given by the enumeration representing the registers
     * 
     * @see java.lang.Enum<E>;
     * @return the ordinal of this enumeration constant (its position in its
     *         enum declaration, where the initial constant is assigned an
     *         ordinal of zero)
     */
    int ordinal();

    /**
     * Returns the same value as ordinal, but with a little more "expressive"
     * name
     * 
     * @return an int : the same value as ordinal, but with a little more
     *         "expressive" name
     */
    default int index() {
        return ordinal();
    }

    /**
     * constructs a mask of the Bit : a value for which the only "1" is the bit
     * that corresponds to the index
     * 
     * @return an int : the mask of the Bit
     */
    default int mask() {
        return Bits.mask(ordinal());
    }
}
