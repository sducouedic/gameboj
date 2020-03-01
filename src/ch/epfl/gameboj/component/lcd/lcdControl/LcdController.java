package ch.epfl.gameboj.component.lcd.lcdControl;

import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;

/**
 * The component that represents the controller of the LCD screen. It calculates
 * and generates via the LCDImageGenerator the final image displayed on the
 * screen. Moreover, for the bonus, the informations containing in the displayed
 * image is not the same depending in which state of displaying we are
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class LcdController implements Clocked, Component {

    /**
     * the width and the height of the LCD screen, in pixel unit
     */
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int MODE2_CYCLES = 20;
    private static final int MODE3_CYCLES = 43;
    private static final int MODE0_CYCLES = 51;
    private static final int MODE1_NB_LINES = 10;

    private final Cpu cpu;
    private final LcdImageGenerator imageGenerator;
    private final RegisterFile<Reg> regs;
    private Bus bus;

    private long nextNonIdleCycle;
    private int lcdOnCycle;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private int copySource;
    private int copyDestination;

    private DisplayMode displayMode;
    private LcdImage statsImage;

    /**
     * This enumeration represents in which mode of visualization of the
     * informations we are
     * 
     * @author Sophie du Couédic (260007)
     * @author Arnaud Robert
     */
    public enum DisplayMode {
        NORMAL, SPRITE, TILES, BACKGROUND
    }

    /**
     * An enum to represents the registers in the LcdController, the class
     * LcdImageGenerator has access to this enum
     * 
     * @author Sophie du Couedic (260007)
     * @author Arnaud Robert (287964)
     */
    enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    /**
     * An enum to represents every single bits of the register LCDC, the class
     * LcdImageGenerator has access to this enum
     * 
     * @author Sophie du Couedic (260007)
     * @author Arnaud Robert (287964)
     */
    enum LCDCBit implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STATBit implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    }

    private enum Mode {
        M0, M1, M2, M3
    }

    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        regs = new RegisterFile<Reg>(Reg.values());

        imageGenerator = new LcdImageGenerator(regs);

        nextNonIdleCycle = 0;
        lcdOnCycle = 0;

        nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);

        copyDestination = AddressMap.OAM_END;

        displayMode = DisplayMode.NORMAL;
        statsImage = null;
    }

    @Override
    public int read(int address) {

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            int index = address - AddressMap.REGS_LCDC_START;
            return regs.get(Reg.values()[index]);
        }

        return imageGenerator.read(address);

    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {

            int index = address - AddressMap.REGS_LCDC_START;

            Reg r = Reg.values()[index];

            switch (r) {
            case STAT:
                regs.set(Reg.STAT, (regs.get(Reg.STAT) & 0b00000111)
                        | (data & 0b11111000));
                break;

            case LY:
                break;

            case LYC:
                regs.set(r, data);
                checkLY_LYC();
                break;

            case LCDC:
                regs.set(Reg.LCDC, data);
                checkLCDC();
                break;

            case DMA:
                copySource = data << Byte.SIZE;
                copyDestination = AddressMap.OAM_START;

            default:
                regs.set(r, data);
            }
        } else
            imageGenerator.write(address, data);
    }

    /**
     * TODO
     *
     * @return
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * a setter for the way of displaying the image and its informations on the
     * screen
     * 
     * @param displayMode
     *            : the new mode of displaying
     */
    public void switchDisplayMode() {
        displayMode = DisplayMode.values()[(displayMode.ordinal() + 1) % 4];
        imageGenerator.resetDrawTile();
    }
    
    public void returnPressed() {
        if (displayMode == DisplayMode.TILES)
            imageGenerator.uploadNewTile();
    }

    public void clickOnScreen(int x, int y) {
        if (displayMode == DisplayMode.SPRITE)
            imageGenerator.setStatsSprites(x, y);
    }

    public void clickOnStatsScreen(int x, int y) {
        if (displayMode == DisplayMode.TILES) {
           imageGenerator.clickOnTileScreen(x,y);
        }
    }

    public void setInformationsMessages(List<String> messages) {
        imageGenerator.setMessage(messages);
    }

    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    @Override
    public void cycle(long cycle) {

        if (nextNonIdleCycle == Long.MAX_VALUE
                && regs.testBit(Reg.LCDC, LCDCBit.LCD_STATUS)) {
            lcdOnCycle = 0;
            reallyCycle();
        }

        if (cycle < nextNonIdleCycle) {
            ++lcdOnCycle;
            return;
        } else {
            reallyCycle();
        }

        if (copyDestination != AddressMap.OAM_END) {
            imageGenerator.write(copyDestination, bus.read(copySource));
            copySource++;
            copyDestination++;
        }

        ++lcdOnCycle;
    }

    public LcdImage currentImage() {
        if (currentImage == null) {
            return new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
        }
        return currentImage;
    }

    public LcdImage statsImage() {
        return statsImage;
    }

    private void reallyCycle() {
        switch (lcdOnCycle) {

        case 0:
        case MODE0_CYCLES + MODE2_CYCLES + MODE3_CYCLES:
            if (regs.get(Reg.LY) == 0) {
                setMode(Mode.M2);
                nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
            }
            if (getMode() != Mode.M1) {
                if (regs.get(Reg.LY) == LCD_HEIGHT) {
                    setMode(Mode.M1);
                    currentImage = nextImageBuilder.build();
                    updateStatsImage();
                    imageGenerator.resetWinY();
                } else
                    setMode(Mode.M2);
            }

            lcdOnCycle = 0;
            nextNonIdleCycle += MODE2_CYCLES;
            break;

        case MODE2_CYCLES:
            if (getMode() != Mode.M1)
                setMode(Mode.M3);

            nextNonIdleCycle += MODE3_CYCLES;
            imageGenerator.computeNormalLine(nextImageBuilder);
            updateLYForNewLine();
            break;

        case MODE2_CYCLES + MODE3_CYCLES:
            if (getMode() != Mode.M1)
                setMode(Mode.M0);
            nextNonIdleCycle += MODE0_CYCLES;
            break;
        }
    }

    private void updateStatsImage() {
        switch (displayMode) {

        case NORMAL:
            statsImage = imageGenerator.computeInformationMessage();
            break;

        case BACKGROUND:
            statsImage = imageGenerator.computeEntireBG();
            break;

        case TILES:
            statsImage = imageGenerator.computeTilesInformations();
            break;

        case SPRITE:
            statsImage = imageGenerator.computeStatsSprites();
            break;
        }
    }

    private void updateLYForNewLine() {
        int tmp = regs.get(Reg.LY);
        if (tmp == LCD_HEIGHT + MODE1_NB_LINES - 1) {
            regs.set(Reg.LY, 0);
        } else {
            regs.set(Reg.LY, tmp + 1);
        }
        checkLY_LYC();
    }

    private void checkLY_LYC() {
        if (regs.get(Reg.LY) == regs.get(Reg.LYC)) {
            setSTATBit(STATBit.LYC_EQ_LY, true);
            if (testSTATBit(STATBit.INT_LYC))
                cpu.requestInterrupt(Interrupt.LCD_STAT);

        } else {
            setSTATBit(STATBit.LYC_EQ_LY, false);
        }
    }

    private void checkSTAT(int oldSTAT) {

        boolean tmp = false;

        if (getMode() == Mode.M0 && getMode(oldSTAT) != Mode.M0
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE0))
            tmp = true;

        if (getMode() == Mode.M2 && getMode(oldSTAT) != Mode.M2
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE2))
            tmp = true;

        if (getMode() == Mode.M1 && getMode(oldSTAT) != Mode.M1
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE1)) {
            tmp = true;
            cpu.requestInterrupt(Interrupt.VBLANK);
        }

        if (tmp)
            cpu.requestInterrupt(Interrupt.LCD_STAT);

    }

    private void checkLCDC() {
        if (!regs.testBit(Reg.LCDC, LCDCBit.LCD_STATUS)) {
            setMode(Mode.M0);
            regs.set(Reg.LY, 0);
            checkLY_LYC();
            nextNonIdleCycle = Long.MAX_VALUE;
        }
    }

    private void setSTATBit(STATBit bit, boolean v) {
        regs.setBit(Reg.STAT, bit, v);
    }

    private boolean testSTATBit(STATBit bit) {
        return regs.testBit(Reg.STAT, bit);
    }

    private Mode getMode(int STAT) {
        Preconditions.checkBits8(STAT);
        int mode = (Bits.test(STAT, STATBit.MODE1) ? 1 : 0) * 2
                + (Bits.test(STAT, STATBit.MODE0) ? 1 : 0);

        return Mode.values()[mode];
    }

    private Mode getMode() {
        return getMode(regs.get(Reg.STAT));
    }

    private void setMode(Mode m) {
        int oldMode = regs.get(Reg.STAT);
        int mode = m.ordinal();

        setSTATBit(STATBit.MODE0, (mode % 2) == 1);
        setSTATBit(STATBit.MODE1, (mode / 2) == 1);

        if (m == Mode.M1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
        checkSTAT(oldMode);
    }
}
