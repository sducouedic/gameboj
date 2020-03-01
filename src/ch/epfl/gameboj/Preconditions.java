package ch.epfl.gameboj;

import java.lang.IllegalArgumentException;

/**
 * A set of useful assertion methods
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public interface Preconditions {
   
    /**
     * checks that a requirement b is fulfilled 
     * 
     * @param b a boolean
     * @throws IllegalArgumentException if b is false
     */
    static void checkArgument(boolean b) throws IllegalArgumentException {
        if(!b) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * checks that the argument is positive and that it can be represented by a byte
     * 
     * @param v an int, the param to be checked
     * @return v the parameter, if v is between 0 and FF
     * @throws IllegalArgumentException if v is not between 0 and FF
     */
    static int checkBits8(int v) throws IllegalArgumentException {
        checkArgument(v >= 0 && v < 0x100);
        return v;
    }
    
    /**
     * checks that the argument is positive and can be represented by 16 bits
     *  
     * @param v an int, the param to be checked
     * @return v the parameter, if v is between 0 and FFFF
     * @throws IllegalArgumentException if v is not between 0 and FFFF
     */
    static int checkBits16(int v) throws IllegalArgumentException {
        checkArgument(v >= 0 && v < 0x10000);
        return v;
    }
    
    
}
