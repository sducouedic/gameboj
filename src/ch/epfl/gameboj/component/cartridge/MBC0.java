package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * A bank memory controller of type 0 : it means that it can contains only a
 * read-only memory of 32768 bytes (= 0x8000 = MB_ROM_SIZE)
 * 
 * @author Sophie du Couédic (26007)
 * @author Arnaud Robert (287964)
 */
public final class MBC0 implements Component {

    private final Rom rom;
    private final static int MB_ROM_SIZE = 0x8000;

    /**
     * Constructs a new bank memory controller that contains the given 32678
     * bytes read-only memory
     * 
     * @param rom
     *            a read-only memory of 32768 bytes
     * @throws IllegalArgumentException
     *             if rom is not of size 32768 bytes
     * @throws NullPointerException
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == MB_ROM_SIZE);
        this.rom = rom;
    }

    /**
     * Implements the method read of Component, that returns the value stored at
     * the given address in the memory, or NO_DATA if the address doesn't belong
     * to the memory
     * 
     * @param address
     *            an int : the address that contains the desired data
     * @return an int (byte) : the value stored at the given address in the
     *         memory
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= MB_ROM_SIZE) {
            return NO_DATA;
        } else {
            return rom.read(address);
        }
    }

    /**
     * Implements the method write of Component, that is supposed to store a
     * value at the given address. Actually, as the memory is a read-only
     * memory, the method doesn't store any value in the memory at the given
     * address
     * 
     * @param address
     *            an int : the address
     * @param data
     *            an int : the value
     * 
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value or if data is not a
     *             8-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#write(int,int)
     */
    @Override
    public void write(int address, int data) {
    }

}
