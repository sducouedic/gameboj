package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * a Component connected to the bus
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public interface Component {

    /**
     * This value will returned by the method read if no data is stored at the
     * address
     */
    public static final int NO_DATA = 0x100;

    /**
     * Returns the value that is stored in the address
     * 
     * @param address
     *            an int : the address in which we want the data
     * @return an int : the data contained in the address
     */
    int read(int address);

    /**
     * Stores a data in the address
     * 
     * @param address
     *            an int : the address where we want to store the data
     * @param data
     *            the data we want to store
     */
    void write(int address, int data);

    /**
     * Attaches the component to the bus
     * 
     * @param bus
     *            a Bus : the bus that will be bounded to the component
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
