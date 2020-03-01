package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * A non-volatile, read-only memory
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Rom {

    private final byte[] memory;
    
    /**
     * constructs the non-volatile memory that contains an array of byte
     * 
     * @param data
     *            : an array of byte which will be copied as the memory
     * @throws NullPointerException if data is null
     */
    public Rom(byte[] data) {
        memory = Arrays.copyOf(data, data.length);
    }

    /**
     * gives the size of the memory
     * 
     * @return an integer : the size of the memory
     */
    public int size() {
        return memory.length;
    }

    /**
     * returns the data contained in the memory at the given index
     * 
     * @param index
     *            an integer : the index of the array that we want the data
     * @return an integer : the data at the given index in the memory
     * @throws IndexOutOfBoundsException
     *             if the index is negative or higher than the memory's length
     */
    public int read(int index) {
        Objects.checkIndex(index, memory.length);
        return Byte.toUnsignedInt(memory[index]);
    }
}
