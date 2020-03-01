package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * The Joypad : the small "keyboard" of the GameBoy that reacts to the pressures
 * on the buttons Left,Right,Up,Down, "A", "B", Start and Select
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class Joypad implements Component {

    private static final int NUMBER_OF_KEYS = 8;

    /**
     * An enum to represents keys of the GameBoy
     * 
     * @author Sophie du Couédic (260007)
     * @author Arnaud Robert (287964)
     *
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    private final Cpu cpu;

    private int P1;

    private final int[] allKeys;

    /**
     * Constructs and return a new Joypad, linked to the given cpu (used to
     * throw the corresponding interruption)
     * 
     * @param cpu
     *            : the CPU of the gameboy
     * @return a new Joypad
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
        P1 = Bits.clip(Byte.SIZE, -1);
        allKeys = new int[NUMBER_OF_KEYS];
    }

    /**
     * Implements the method read of Component, that returns the value stored in
     * the register P1, or NO_DATA if the address doesn't belong the register P1
     * 
     * @param address
     *            : the address that contains the desired data
     * @return an integer (byte) : the value stored at P1 register or NO_DATA if
     *         the address does not corresponds to P1
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_P1) {
            return P1;
        }
        return NO_DATA;
    }

    /**
     * Implements the method write of Component, that stores a value in the
     * register P1, if the address corresponds to it. This method also adapt the
     * state of P1 and the reaction of the gameboy if one of the keys is pressed.
     * 
     * @param address
     *            the address
     * @param data
     *            the value to store
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value or if data is not a
     *             8-bits value
     * @see ch.epfl.gameboj.component.Component#write(int,int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            data = (data >>> 4) << 4;
            P1 = Bits.set(Bits.set(P1, 4, false), 5, false);
            P1 = P1 | data;

            int oldP1 = P1;
            updateP1();
            compareOldP1AndNewP1(oldP1);
        }
    }

    /**
     * This method simulates the effect in the gameboy, when someone press one
     * of the buttons of the gameboy
     * 
     * @param key
     *            : the key of the gameboy that is pressed
     */
    public void keyPressed(Key key) {
        int keyIndex = key.ordinal();
        allKeys[keyIndex] = Bits.set(0, keyIndex, true);
    }

    /**
     * This method simulates the effect in the gameboy, when someone release the
     * pressure of a key of the gameboy
     * 
     * @param key
     *            : the key of the gameboy the is released
     */
    public void keyReleased(Key key) {
        int keyIndex = key.ordinal();
        allKeys[keyIndex] = 0;
    }

    private void updateP1() {
        int tmp1 = 0;

        if (!Bits.test(P1, 4)) {
            for (int i = 0; i < NUMBER_OF_KEYS / 2; ++i) {
                tmp1 |= allKeys[i];
            }
        }

        if (!Bits.test(P1, 5)) {
            for (int i = NUMBER_OF_KEYS / 2; i < NUMBER_OF_KEYS; ++i) {
                tmp1 |= (allKeys[i] >>> (NUMBER_OF_KEYS / 2));
            }
        }

        int tmp2 = (~P1 >>> 4) << 4;
        P1 = ~(tmp1 | tmp2);
    }

    private void compareOldP1AndNewP1(int oldP1) {
        oldP1 = Bits.clip(4, oldP1);
        int newP1 = Bits.clip(4, P1);

        if (((oldP1 ^ newP1) & oldP1) != 0) {
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
    }
}
