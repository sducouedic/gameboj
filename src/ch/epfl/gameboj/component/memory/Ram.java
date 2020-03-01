package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * A random-access memory
 * 
 * @author Sophie Du Couedic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class Ram {
    
    private final byte[] memory;
    
    /**
     * Constructs a RAM memory with a given size
     * 
     * @param size
     * @throws IllegalArgumentException
     *             if the parameter size is negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        memory = new byte[size];
    }

    /**
     * Returns the RAM's size (in bytes)
     * 
     * @return an int : the RAM's size
     */
    public int size() {
        return memory.length;
    }

    /**
     * Returns the byte located at the given index as a value between 0 and
     * 0xFF, or throws an exception if index is invalid
     * 
     * @param index
     *            the index
     * @return a positive value between 0 and 0xFF
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than the memory's length
     */
    public int read(int index) {
        Objects.checkIndex(index, size());
        return Byte.toUnsignedInt(memory[index]);
    }

    /**
     * Writes the given value in the given place of the RAM memory, throws
     * exceptions if the index or the value are invalid
     * 
     * @param index
     *            the index
     * @param value
     *            the value to be written
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than the memory's length
     * @throws IllegalArgumentException
     *             if the value is not an 8-bits value
     */
    public void write(int index, int value) {
        Objects.checkIndex(index, size());
        Preconditions.checkBits8(value);
        memory[index] = (byte) value;
    }
}
