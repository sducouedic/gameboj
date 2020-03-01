package ch.epfl.gameboj.component;

/**
 * An object that depends on a clock
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 *
 */
public interface Clocked {

    /**
     * Asks the component to evolve by executing all operations it is supposed
     * to execute during the cycle given as parameter
     * 
     * @param cycle
     *            the current the cycle
     */
    abstract void cycle(long cycle);
}
