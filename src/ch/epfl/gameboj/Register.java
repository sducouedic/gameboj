package ch.epfl.gameboj;


/**
 * A Register
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 *
 */
public interface Register {

    /**
     * Is given by the enumeration representing the registers
     * 
     * @see java.lang.Enum<E>;
     * @return the ordinal of this enumeration constant (its position in its
     *         enum declaration, where the initial constant is assigned an
     *         ordinal of zero)
     */
    int ordinal();

    /**
     * Returns the same value as the method ordinal()
     * 
     * @return the ordinal of this enumeration constant (its position in its
     *         enum declaration, where the initial constant is assigned an
     *         ordinal of zero)
     */
    public default int index() {
        return ordinal();
    }
}
