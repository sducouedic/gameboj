package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * The controller of a boot memory (read-only)
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class BootRomController implements Component {

    private final Cartridge cart;

    private boolean bootRomDisabled = false;

    private final Rom bootRom = new Rom(BootRom.DATA);

    /**
     * Constructs and returns a new BootRomController bounded to the given
     * cartridge
     * 
     * @param cartridge
     *            Cartridge
     * @throws nullpointerException
     *             if the cartridge is null
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        cart = cartridge;
    }

    /**
     * Implements the method read of Component, that returns the value stored at
     * the given address in the memory, or NO_DATA if the address doesn't belong
     * to the memory. If the address is in the range from 0x0 (BOOT_ROM_START)
     * to 0x100 (excluded, BOOT_ROM_END) and if the boot memory is not disabled,
     * the returned value will be the value from the boot memory at the given
     * address, otherwise it will be the value from the cartridge
     * 
     * @param address
     *            integer : the address that contains the desired data
     * @return integer (byte) : the value stored at the given address in the
     *         memory
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (AddressMap.BOOT_ROM_START <= address
                && address < AddressMap.BOOT_ROM_END) {
            return bootRomDisabled ? cart.read(address) : bootRom.read(address);
        }
        return cart.read(address);
    }

    /**
     * Implements the method write of Component. The given value is stored in
     * the cartridge at the given address. The only exception is for the address
     * 0xFF50 : for any data value, if the write method is called for this
     * address, the boot rom is disabled and no value will be written at the
     * address
     * 
     * @param address
     *            integer : the address
     * @param data
     *            integer : the value
     * 
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value or if data is not a
     *             8-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#write(int,int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomDisabled = true;
        } else {
            cart.write(address, data);
        }

    }
}