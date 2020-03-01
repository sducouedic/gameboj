package ch.epfl.gameboj;

import java.io.IOException;
import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.lcdControl.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * The GameBoy itself
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class GameBoy {

    private final Bus bus;
    private final BootRomController brc;
    private final RamController workRam;
    private final RamController echoRam;
    private final Cpu cpu;
    private final LcdController lcd;
    private final Timer timer;
    private final Joypad joypad;
    
    private long cycleGB;

    public final static long NUMBER_OF_CYCLES_PER_SECOND = 1 << 20;
    public final static double NUMBER_OF_CYCLE_PER_NANOSECOND = NUMBER_OF_CYCLES_PER_SECOND/1e9;

            
    /**
     * Builds a GameBoy and its different components and then proceeds to attach
     * those to the GameBoy
     * 
     * @param cartridge
     *            : The cartridge used to build the GameBoy, access to its
     *            information will allow the GameBoy to run the game contained
     *            inside
     * @throws NullPointerException
     *             if cartridge is null
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);

        bus = new Bus();

        brc = new BootRomController(cartridge);

        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);

        workRam = new RamController(ram, AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START,
                AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        lcd = new LcdController(cpu);
        timer = new Timer(cpu);
        joypad = new Joypad(cpu);

        cycleGB = 0;

        cpu.attachTo(bus);
        lcd.attachTo(bus);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
        brc.attachTo(bus);
        timer.attachTo(bus);
        joypad.attachTo(bus);
    }

    /**
     * Getter for the GameBoy's bus
     * 
     * @return the current GameBoy's bus
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Getter for the GameBoy's cpu
     * 
     * @return the current GameBoy's cpu
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Getter for the GameBoy's lcdController
     * 
     * @return the current GameBoy's lcdController
     */
    public LcdController lcdController() {
        return lcd;
    }

    /**
     * Getter for the GameBoy's timer
     * 
     * @return the current GameBoy's timer
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * Getter for the GameBoy's joypad
     * 
     * @return the current GameBoy's joypad
     */
    public Joypad joypad() {
        return joypad;
    }

    /**
     * Simulates the operation of the GameBoy until the given cycle minus one
     * 
     * @param cycle
     *            the value of the next cycle to be ran after the method is used
     *            (run until this one)
     * 
     * @throws IllegalArgumentException
     *             if a strictly higher number of cycles has already been
     *             simulated
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycleGB <= cycle);

        while (cycleGB < cycle) {
            timer.cycle(cycleGB);
            cpu.cycle(cycleGB);
            lcd.cycle(cycleGB);
            cycleGB++;
        }
    }

    /**
     * Returns the number of cycles that have already been simulated
     * 
     * @return the number of cycles that have already been simulated
     */
    public long cycles() {
        return cycleGB;
    };

}
