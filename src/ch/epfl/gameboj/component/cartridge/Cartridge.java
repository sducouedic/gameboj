package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a GameBoy program in the form of a cartridge of type 0 (with a
 * memory of 32768 bytes)
 *
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Cartridge implements Component {

    private final Component mbc;
    private final static int CARTRIDGE_TYPE_ADDRESS = 0x147;
    private final static int[] MBC1_RAM_SIZE = {0,2048,8192,3276};
    private final static int MBC1_RAM_SIZE_ADDRESS = 0x149;

    private Cartridge(Component mbc) {
        this.mbc = Objects.requireNonNull(mbc);
    }

    /**
     * Implements the method read of Component, that returns via the bank memory
     * controller the value stored at the given address in the memory, or
     * NO_DATA if the address doesn't belong to the memory
     * 
     * @param address
     *            : an integer the address that contains the desired data
     * @return an integer (byte) : the value stored at the given address in the
     *         memory
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    /**
     * Implements the method write of Component, that is supposed to store a
     * value at the given address. But as the memory of the cartridge is a
     * read-only memory, the method actually doesn't store any value in the
     * memory at the given address
     * 
     * @param address
     *            an integer : the address
     * @param data
     *            an integer : the value
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value or if data is not a
     *             8-bits value
     * @see ch.epfl.gameboj.component.Component#write(int,int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        mbc.write(address, data);
    }

    /**
     * Constructs and returns a new Cartridge of type 0 which the read-only
     * memory contains the bytes of the given file (the file must contain 0 at
     * position 0x147 (CARTRIDGE_TYPE_ADDRESS) and must be of the size of 32768
     * bytes)
     * 
     * @param romFile
     *            a File : the file with the required bytes
     * @return a new Cartridge containing the bytes of the file
     * @throws IOException
     *             if an I/O error occurs or if the file doesn't exist
     * @throws IllegalArgumentException
     *             if the file doesn't contain 0 at position 0x147 or is not of
     *             size 32768 bytes
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream s = new FileInputStream(romFile)) {
            byte[] tab = new byte[(int) romFile.length()];
            tab = s.readAllBytes().clone();

            int cartridgeType = Byte.toUnsignedInt(tab[CARTRIDGE_TYPE_ADDRESS]);
            Preconditions
                    .checkArgument(cartridgeType >= 0 && cartridgeType <= 3);
            
            Cartridge cart;
            switch(cartridgeType) {
                case 0 : cart = new Cartridge(new MBC0(new Rom(tab))); break;
                case 1 : cart = new Cartridge(new MBC1(new Rom(tab),MBC1_RAM_SIZE[0])); break;
                case 2 : 
            case 3:
                cart = new Cartridge(new MBC1(new Rom(tab),
                        MBC1_RAM_SIZE[tab[MBC1_RAM_SIZE_ADDRESS]])); break;
                default : throw new Error();
            }
            
            return cart;

        } catch (FileNotFoundException a) {
            throw new IOException();
        }
    }
}
