package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * a Timer that measures the passage of time in a processor
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Timer implements Component, Clocked {

    private final Cpu cpu;

    private int DIV;
    private int TIMA;
    private int TMA;
    private int TAC;

    private final static int TIMA_MAX_VALUE = 0xFF;

    /**
     * Constructs a new timer associated to the given processor
     * 
     * @param cpu
     *            : the CPU associated to the timer
     * @throws NullPointerException
     *             if the given cpu is null
     */
    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        DIV = 0;
        TIMA = 0;
        TMA = 0;
        TAC = 0;
    }

    /**
     * Implements the method cycle of Clocked. The timer evolves to the given
     * cycle, according to a determined process : __1. the main counter is
     * incremented by 4 units, and loops to zero if it reaches it's max value
     * 0xFFFF __2. If it is not disabled, the second counter TIMA is incremented
     * by 1 if a bit (given by the register TAC) of the main counter goes from
     * '1' to '0' __3. If TIMA reaches his max value (0xFF), it raises the TIMER
     * interruption of the cpu and is reset to the value stored in the register
     * TMA
     * 
     * @param cycle
     *            a long : the current cycle
     * @see ch.epfl.gameboj.component#cycle(long)
     */
    @Override
    public void cycle(long cycle) {

        boolean previousState = state();

        manageDIV();

        incTIMAIfChange(previousState);
    }

    /**
     * Implements the method write of Component. The given value is stored in
     * one (or none) of the Timer's register depending to the given address. If
     * the address corresponds to the register TMA, TAC or TIMA, the register
     * will take the given value, it it corresponds to the main counter, then
     * the counter will be reset to 0 instead of taking the given value. __TIMA
     * is incremented if it is not disabled (according to TAC register) and if a
     * bit (given by the register TAC) of the main counter goes from '1' to '0'.
     * TIMA raises the TIMA exception of the cpu and reset to the value
     * contained in TMA if it reaches his max value (0xFF)
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
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);

        boolean previousState = state();

        switch (address) {

        case AddressMap.REG_DIV:
            DIV = 0;
            break;

        case AddressMap.REG_TIMA:
            TIMA = data;
            break;

        case AddressMap.REG_TMA:
            TMA = data;
            break;

        case AddressMap.REG_TAC:
            TAC = data;
            break;

        default:
            break;
        }

        incTIMAIfChange(previousState);
    }

    /**
     * Implements the method read of Component. Returns the value of the
     * register corresponding to the given address, returns NO_DATA if no
     * register corresponds to the address. If the address corresponds to the
     * main counter, only the eights most significants bits of the main counter
     * will be returned.
     * 
     * @param address
     *            an int : the address that contains the desired data
     * @return an int (byte) : the value stored at the given address in the
     *         memory
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {

        Preconditions.checkBits16(address);

        switch (address) {

        case AddressMap.REG_DIV:
            return Bits.extract(DIV, Byte.SIZE, Byte.SIZE);

        case AddressMap.REG_TIMA:
            return TIMA;

        case AddressMap.REG_TMA:
            return TMA;

        case AddressMap.REG_TAC:
            return TAC;

        default:
            return NO_DATA;
        }

    }

    private void manageDIV() {
        DIV = Bits.clip(Short.SIZE, DIV + 4);
    }

    private void incTIMAIfChange(boolean previousState) {
        boolean newState = state();

        if (previousState && !newState) {
            ++TIMA;

            if (TIMA > TIMA_MAX_VALUE) {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            }
        }
    }

    private boolean state() {

        int DIVBitToTest;

        switch (Bits.extract(TAC, 0, 2)) {

        case 0b00:
            DIVBitToTest = 9;
            break;
        case 0b01:
            DIVBitToTest = 3;
            break;
        case 0b10:
            DIVBitToTest = 5;
            break;
        case 0b11:
            DIVBitToTest = 7;
            break;
        default:
            throw new Error("no bit to test in the counter");
        }

        return Bits.test(TAC, 2) && Bits.test(DIV, DIVBitToTest);
    }

}
