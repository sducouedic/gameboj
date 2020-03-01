package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * A RegisterFile
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 *
 */

public final class RegisterFile<E extends Register> {

    private final int[] file;

    /**
     * Builds a register file with registers of length 8. The file's size (i.e
     * the number of registers) is equal to that of the given array
     * 
     * This constructor is bound to always be called with the result of the
     * method values from the enumeration representing the registers
     * 
     * @param allRegs
     *            an array obtained with method value applied to an enumeration
     *            representing the registers
     */
    public RegisterFile(E[] allRegs) {
        file = new int[allRegs.length];
    }

    /**
     * Returns the 8-bits value contained in the given register in the form of
     * an integer between 0 (included) and FF16 (included)
     * 
     * @param reg
     *            a register
     * @return the 8-bits value contained in the given register in the form of
     *         an integer between 0 (included) and FF16 (included)
     */
    public int get(E reg) {
        return file[reg.index()];
    }

    /**
     * Modifies the content of the given register in order to have it equal to
     * the given 8 bits value
     * 
     * @param reg
     *            the register whose value will be setS
     * @param newValue
     *            the newValue to be set
     * @throws IllegalArgumentException
     *             if newValue is not a valid 8-bit value
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);

        file[reg.index()] = newValue;
    }

    /**
     * Returns true if and only if the given bit of the given register is equal
     * to 1
     * 
     * @param reg
     *            the register to test
     * @param b
     *            the bit to test
     * @return true if and only if the given bit of the given register is equal
     *         to 1
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }

    /**
     * Modifies the value contained in the given register in order to have the
     * given bit be equal to the given value
     * 
     * @param reg
     *            the register
     * @param bit
     *            the bit to be set
     * @param newValue
     *            the newValue to set
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        file[reg.index()] = Bits.set(get(reg), bit.index(), newValue);
    }
}
