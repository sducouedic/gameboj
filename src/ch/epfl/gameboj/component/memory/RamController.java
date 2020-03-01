package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * a RamController that controls the access to the ram
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class RamController implements Component {

    private final Ram ram;
    private final int startAddress;
    private final int endAddress;

    /**
     * constructs a new Ram controller that will be bounded to a ram and control
     * the accesses to it the ram will be accessible via the ramController from
     * startAddress (included) to endAddress (excluded)
     * 
     * @param ram
     *            a Ram
     * @param startAddress
     *            an int the starting address of the Ram
     * @param endAddress
     *            an int the end address of the Ram
     * @throws NullPointerException
     *             if the ram is null
     * @throws IllegalArgumentException
     *             if startAddress or enAddress is not a 16-bits value
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        this.ram = Objects.requireNonNull(ram);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Preconditions.checkArgument((endAddress - startAddress >= 0)
                && (endAddress - startAddress <= ram.size()));
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    /**
     * constructs a new Ram controller that will be bounded to a ram. The
     * entirely datas of the ram will be accessible so we just need to know the
     * startAddress and the size of the memory
     * 
     * @param ram
     *            a Ram
     * @param startAddress
     *            an int the starting address of the map
     * @throws NullPointerException
     *             if the ram is null
     * @throws IllegalArgumentException
     *             if startAddress is not a 16-bits value
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

    /**
     * implements the method read of component : returns the value that is
     * stored in the ram at the address returns NO_DATA if the address doesn't
     * below to the ramController
     * 
     * @param address
     *            an int
     * @return an int : the data we are looking for or NO_DATA
     * @throws IllegalArgumentException if address is not a 16-bits value
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < startAddress || address >= endAddress) {
            return NO_DATA;
        }
            return ram.read(address - startAddress);
    }

    /**
     * Implements the method write of component : store the data in the ram at
     * the address does nothing if the address doesn't below to the
     * ramController
     * 
     * @param address
     *            an int : the address at which we store the data
     * @param data
     *            an int : the data we want to store
     * @throws IllegalArgumentException
     *             if address is not a valid 16 bits value or if data is not a
     *             valid 8 bits value
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= startAddress && address < endAddress) {
            ram.write(address - startAddress, data);
        }
    }
}
