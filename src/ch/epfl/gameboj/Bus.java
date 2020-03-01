package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * A bus connecting different components
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 *
 */
public final class Bus {

    private final ArrayList<Component> components;

    /**
     * constructs a bus and initializes an empty arraylist
     */
    public Bus() {
        components = new ArrayList<Component>();
    }

    /**
     * attaches himself to a component, by adding it to his list of components
     * throws NullPointerException if the component is null
     * 
     * @param component
     *            : the component to attach
     * @throws NullPointerException
     *             if component is null
     */
    public void attach(Component component) {
        components.add(Objects.requireNonNull(component));
    }

    /**
     * returns the value stored at the address if at least one component has a
     * data at the address
     * 
     * @param address
     *            an int : the address
     * @return an int : the data at the address
     * @throws IllegalArgumentException
     *             if the address
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        for (Component c : components) {
            int value = c.read(address);
            if (value != Component.NO_DATA) {
                return value;
            }
        }
        return 0xFF;
    }

    /**
     * store the data at the address in every component bouned to the bus
     * 
     * @param address
     *            an int : the address we want to store the new data
     * @param data
     *            an int
     * @throws IllegalArgumentException
     *             if address or data is not a 8-bits value
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (Component c : components) {
            c.write(address, data);
        }
    }
}
