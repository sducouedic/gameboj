package ch.epfl.gameboj.component.lcd.lcdControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.component.lcd.LcdImageLine;
import ch.epfl.gameboj.component.lcd.LcdImageLine.Builder;
import ch.epfl.gameboj.component.lcd.lcdControl.LcdController.LCDCBit;
import ch.epfl.gameboj.component.lcd.lcdControl.LcdController.Reg;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import ch.epfl.gameboj.component.memory.Rom;

public final class LcdImageGenerator implements Component {

    private static final int LCD_WIDTH = LcdController.LCD_WIDTH;
    private static final int LCD_HEIGHT = LcdController.LCD_HEIGHT;
    private static final int IMAGE_DIMENSION = 256;

    private static final int TILES_CHOICES_PER_IMAGE = 256;
    private static final int TILE_DIMENSION = 8;
    private static final int OCTETS_INFOS_PER_TILE = 16;

    private static final int NUMBER_OF_SPRITES = 40;
    private static final int NUMBER_OF_OCTETS_PER_SPRITE = AddressMap.OAM_RAM_SIZE
            / NUMBER_OF_SPRITES;
    private static final int MAX_NUMBER_OF_SPRITES_PER_LINE = 10;

    private static final int Y_AXIS_DELAY = 16;
    private static final int X_AXIS_DELAY = 8;
    private static final int WX_DELAY = 7;

    private static final int STATS_TILES_WIDTH = 16;
    private static final int STATS_TILES_HEIGHT = 24;
    private static final int DRAW_PIXEL_DIMENSION = 24;
    private static final int DRAW_DIMENSION = DRAW_PIXEL_DIMENSION
            * TILE_DIMENSION;

    private static final String SPRITES_STATS_MESSAGE = "CLICK ON THE SPRITES";
    private static final String EMPTY_LINE_SIZE_32 = "                                ";

    private final Rom charactersTiles;
    private final RamController OAM;
    private final RamController videoRam;
    private final RegisterFile<Reg> regs;

    private int winY;
    private boolean drawTile;
    private int selectedDrawTile;
    private int[] tilePixels;

    private Set<Integer> statsSprites;
    private int spriteInformation;

    private List<String> messages;

    private enum ImageType {
        BACKGROUND, WINDOW, SPRITE_BG, SPRITE_FG
    }

    private enum SpriteAttribute {
        Y, X, TILE, SPECIAL
    }

    private enum SPECIALBit implements Bit {
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    public LcdImageGenerator(RegisterFile<Reg> regs) {
        OAM = new RamController(new Ram(AddressMap.OAM_RAM_SIZE),
                AddressMap.OAM_START, AddressMap.OAM_END);
        videoRam = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE),
                AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);

        charactersTiles = new Rom(CharactereTiles.charactereTiles());

        this.regs = regs;

        statsSprites = new HashSet<>();
        spriteInformation = NUMBER_OF_SPRITES;

        messages = new LinkedList<>();

        drawTile = false;
        selectedDrawTile = 0;
        tilePixels = new int[OCTETS_INFOS_PER_TILE];
    }

    public void resetWinY() {
        winY = 0;
    }

    public void resetDrawTile() {
        drawTile = false;
    }

    public void clickOnTileScreen(int x, int y) {

        if (!drawTile) {
            int xInTileUnits = x / TILE_DIMENSION;
            int yInTileUnits = y / TILE_DIMENSION;

            Preconditions.checkArgument(xInTileUnits < STATS_TILES_WIDTH
                    && yInTileUnits < STATS_TILES_HEIGHT);

            selectedDrawTile = yInTileUnits * STATS_TILES_WIDTH + xInTileUnits;
            drawTile = true;

            for (int i = 0; i < OCTETS_INFOS_PER_TILE; i += 2) {
                int tileName = selectedDrawTile;
                boolean tileSource = true;

                if (tileName >= TILES_CHOICES_PER_IMAGE) {
                    tileName -= TILES_CHOICES_PER_IMAGE;
                    tileSource = false;
                }

                tilePixels[i] = getTileLineMsb(i / 2, tileName, false, false,
                        tileSource);
                tilePixels[i + 1] = getTileLineLsb(i / 2, tileName, false,
                        false, tileSource);
            }

        } else {
            int xInCaseUnits = x / DRAW_PIXEL_DIMENSION;
            int yInCaseUnits = y / DRAW_PIXEL_DIMENSION;

            boolean mBit = Bits.test(tilePixels[yInCaseUnits * 2], xInCaseUnits);
            boolean lBit = Bits.test(tilePixels[yInCaseUnits * 2 + 1],
                    xInCaseUnits);

            boolean newMBit = false, newLBit = false;

            if (mBit && lBit) {
                newMBit = false;
                newLBit = false;
            }

            if (!mBit && lBit) {
                newMBit = true;
                newLBit = false;
            }

            if (mBit && !lBit) {
                newMBit = true;
                newLBit = true;
            }

            if (!mBit && !lBit) {
                newMBit = false;
                newLBit = true;
            }

            tilePixels[yInCaseUnits * 2] = Bits.set(tilePixels[yInCaseUnits * 2],
                    xInCaseUnits, newMBit);
            tilePixels[yInCaseUnits * 2 + 1] = Bits
                    .set(tilePixels[yInCaseUnits * 2 + 1], xInCaseUnits, newLBit);
        }
    }
    
    public void uploadNewTile() {
        if (drawTile) {
            int tileName = selectedDrawTile;
            boolean tileSource = true;
            
            if (tileName >= TILES_CHOICES_PER_IMAGE) {
                tileName -= TILES_CHOICES_PER_IMAGE;
                tileSource = false;
            }
            
            for(int i = 0; i < TILE_DIMENSION; i++) {
                
                int addresse = getTileLineAddress(i, tileName, false, false, tileSource);
                
                videoRam.write(addresse, Bits.reverse8(tilePixels[2*i]));
                videoRam.write(addresse + 1, Bits.reverse8(tilePixels[2*i + 1]));
            }
            
            drawTile = false;
        }
    }

    // set which sprites will be displayed the information on screen
    public void setStatsSprites(int x, int y) {

        int[] spritesOnY = spritesIntersectingLine(y);

        Integer sprite = null;
        for (int s : spritesOnY) {
            int xOfsprite = getAttribute(s, SpriteAttribute.X) - X_AXIS_DELAY;
            if (xOfsprite < x && xOfsprite + TILE_DIMENSION > x)
                sprite = s;
        }

        if (sprite != null) {

            spriteInformation = sprite;

            if (!statsSprites.add(sprite)) {
                statsSprites.remove(sprite);
                spriteInformation = NUMBER_OF_SPRITES;
            }
        }
    }

    public void setMessage(List<String> messages) {
        Preconditions.checkArgument(
                messages.size() <= LCD_HEIGHT / TILE_DIMENSION / 2);
        int longestM = 0;
        for (int m = 0; m < messages.size(); m++) {
            int length = messages.get(m).length();
            if (length > longestM)
                longestM = length;
        }

        Preconditions.checkArgument(longestM <= LCD_WIDTH);
        int to32 = 32 - (longestM % 32);
        if (to32 != 32) {
            longestM += to32;
        }

        List<String> tmp = new ArrayList<String>();
        for (String m : messages) {

            StringBuilder emptyLine = new StringBuilder();
            for (int i = 0; i < longestM / 32; i++) {
                emptyLine.append(EMPTY_LINE_SIZE_32);
            }
            tmp.add(emptyLine.toString());

            tmp.add(multiple32String(m, longestM));
        }

        StringBuilder emptyLine = new StringBuilder();
        for (int i = 0; i < longestM / 32; i++) {
            emptyLine.append(EMPTY_LINE_SIZE_32);
        }
        tmp.add(emptyLine.toString());

        this.messages = tmp;

    }

    @Override
    public int read(int address) {
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
            return OAM.read(address);
        else
            return videoRam.read(address);
    }

    @Override
    public void write(int address, int data) {
        OAM.write(address, data);
        videoRam.write(address, data);
    }

    // Compute the line for the normal lcd screen
    public void computeNormalLine(LcdImage.Builder nextImageBuilder) {
        int bitLineInLCD = regs.get(Reg.LY);
        int adjustedWX = Math.max(regs.get(Reg.WX) - WX_DELAY, 0);

        // constante
        if (bitLineInLCD < LCD_HEIGHT) {

            int bitLine = (bitLineInLCD + regs.get(Reg.SCY)) % IMAGE_DIMENSION;

            LcdImageLine finalLine = new LcdImageLine.Builder(LCD_WIDTH)
                    .build();

            // Background management

            if (testLCDCBit(LCDCBit.BG)) {
                finalLine = backgroundLine(bitLine)
                        .extractWrapped(regs.get(Reg.SCX), LCD_WIDTH);
            }

            // Window management

            if (testLCDCBit(LCDCBit.WIN)
                    && (adjustedWX >= 0 && adjustedWX < LCD_WIDTH)
                    && regs.get(Reg.WY) <= bitLineInLCD) {
                LcdImageLine lineOfZeros = new LcdImageLine.Builder(
                        IMAGE_DIMENSION).build();
                LcdImageLine adjustedWindowLine = lineOfZeros
                        .below(windowLine(winY),
                                new BitVector(IMAGE_DIMENSION, true))
                        .shift(adjustedWX).extractWrapped(0, LCD_WIDTH);

                finalLine = finalLine.join(adjustedWindowLine, adjustedWX);
                winY++;
            }

            BitVector BGWINOpacity = finalLine.opacity();
            LcdImageLine BGSprites = new LcdImageLine.Builder(LCD_WIDTH)
                    .build();
            BitVector BGSpritesOpacity = new BitVector(LCD_WIDTH);

            // Sprites management

            if (testLCDCBit(LCDCBit.OBJ)) {

                int[] allSprites = spritesIntersectingLine(bitLineInLCD);

                BGSprites = backGroundSprites(bitLineInLCD, allSprites);
                BGSpritesOpacity = BGSprites.opacity();

                LcdImageLine FGSprites = foreGroundSprites(bitLineInLCD,
                        allSprites);

                finalLine = finalLine.below(FGSprites);
            }

            // This prevents background sprites and background/window image bits
            // to be both transparents
            BitVector bothTransparents = BGSpritesOpacity.or(BGWINOpacity)
                    .not();
            finalLine = BGSprites.below(finalLine,
                    bothTransparents.or(BGWINOpacity));

            if (!finalLine.opacity().equals(new BitVector(LCD_WIDTH, true)))
                throw new Error();

            nextImageBuilder.setLine(bitLineInLCD, finalLine);
        }
    }

    // Compute the image for the statsImage in the displayState : BACKGROUND
    public LcdImage computeEntireBG() {
        LcdImage.Builder b = new LcdImage.Builder(IMAGE_DIMENSION,
                IMAGE_DIMENSION);

        for (int i = 0; i < IMAGE_DIMENSION; ++i) {
            b.setLine(i, backgroundLine(i));
        }

        LcdImage border = LcdImage.createRectangleBorder(IMAGE_DIMENSION,
                IMAGE_DIMENSION, regs.get(Reg.SCX), regs.get(Reg.SCY),
                LCD_WIDTH, LCD_HEIGHT);
        return b.build().below(border);
    }

    public LcdImage computeTilesInformations() {
        if (!drawTile) 
            return computeAllTiles();
        
        else 
            return computeDrawTilePixels();
    }

    private LcdImage computeDrawTilePixels() {
        LcdImage.Builder imBuilder = new LcdImage.Builder(DRAW_DIMENSION, DRAW_DIMENSION);

        for (int vertical = 0; vertical < TILE_DIMENSION; ++vertical) {
            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(DRAW_DIMENSION);
            
            for(int horizontal = 0; horizontal < TILE_DIMENSION; ++horizontal) {
                
                int nbBytePerPixel = DRAW_PIXEL_DIMENSION/TILE_DIMENSION;
                for(int i = 0; i < nbBytePerPixel; i++) {
                    
                    int msbByte = Bits.test(tilePixels[vertical*2], horizontal) ? 255 : 0;
                    int lsbByte = Bits.test(tilePixels[vertical*2+1], horizontal) ? 255 : 0;
                    
                    lineBuilder.setBytes(horizontal * nbBytePerPixel + i, msbByte, lsbByte);
                }
            }
            
            LcdImageLine line = lineBuilder.build();
            
            for(int i = 0; i < DRAW_PIXEL_DIMENSION; i++) {
                imBuilder.setLine(vertical * DRAW_PIXEL_DIMENSION + i, line);
            }
        }
        return imBuilder.build();
    }

    private LcdImage computeAllTiles() {

        int width = STATS_TILES_WIDTH * TILE_DIMENSION;
        int height = STATS_TILES_HEIGHT * TILE_DIMENSION;

        LcdImage.Builder imageBuilder = new LcdImage.Builder(width, height);

        for (int tileLine = 0; tileLine < STATS_TILES_HEIGHT; tileLine++) {
            for (int lineInTheTile = 0; lineInTheTile < TILE_DIMENSION; lineInTheTile++) {

                LcdImageLine.Builder lineBuilder = new Builder(width);

                for (int columnTile = 0; columnTile < STATS_TILES_WIDTH; columnTile++) {

                    int tileName = tileLine * STATS_TILES_WIDTH + columnTile;
                    boolean tileSource = true;

                    if (tileName >= TILES_CHOICES_PER_IMAGE) {
                        tileName -= TILES_CHOICES_PER_IMAGE;
                        tileSource = false;
                    }

                    int msb = getTileLineMsb(lineInTheTile, tileName, false,
                            false, tileSource);
                    int lsb = getTileLineLsb(lineInTheTile, tileName, false,
                            false, tileSource);

                    lineBuilder.setBytes(columnTile, msb, lsb);
                }

                int currentLine = tileLine * TILE_DIMENSION + lineInTheTile;
                imageBuilder.setLine(currentLine, lineBuilder.build());
            }
        }

        int borderHeight = (TILES_CHOICES_PER_IMAGE / STATS_TILES_WIDTH)
                * TILE_DIMENSION;
        int yCoord = testLCDCBit(LCDCBit.TILE_SOURCE) ? 0 : height / 3 - 1;
        LcdImage border = LcdImage.createRectangleBorder(width, height, 0,
                yCoord, width, borderHeight);

        return imageBuilder.build().below(border);
    }

    // Draw the image that contains all of the sprite (designed by statsSprite)
    // informations
    public LcdImage computeStatsSprites() {
        LcdImage.Builder b = new LcdImage.Builder(LCD_WIDTH,
                LCD_HEIGHT + TILE_DIMENSION * 2);

        int fLine = 0;
        for (int l = 0; l < TILE_DIMENSION; ++l) {
            b.setLine(l, computeMessageLine(l, SPRITES_STATS_MESSAGE));
        }
        fLine += TILE_DIMENSION;

        for (int l = 0; l < LCD_HEIGHT; l++) {
            LcdImageLine line = new LcdImageLine.Builder(LCD_WIDTH).build();

            for (int s : statsSprites) {
                line = line.below(individualSprite(s, l));
            }

            b.setLine(l + fLine, line);
        }
        fLine += LCD_HEIGHT;

        if (spriteInformation != NUMBER_OF_SPRITES) {

            String name = "S" + String.format("%2d", spriteInformation);

            String xCoord = String.format("%3d",
                    getAttribute(spriteInformation, SpriteAttribute.X));
            String yCoord = String.format("%3d",
                    getAttribute(spriteInformation, SpriteAttribute.Y));

            String message = multiple32String(
                    name + " " + xCoord + "X" + yCoord,
                    LCD_WIDTH / TILE_DIMENSION);
            for (int l = 0; l < TILE_DIMENSION; l++) {
                b.setLine(l + fLine, computeMessageLine(l, message));
            }
        }

        return b.build();
    }

    public LcdImage computeInformationMessage() {

        int height = messages.size() * TILE_DIMENSION;

        if (height == 0) {
            return new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
        }

        int width = messages.get(0).length() * TILE_DIMENSION;

        LcdImage.Builder messageBuilder = new LcdImage.Builder(width, height);
        int j = 0;
        for (String message : messages) {
            for (int line = 0; line < TILE_DIMENSION; line++) {
                messageBuilder.setLine(j * TILE_DIMENSION + line,
                        computeMessageLine(line, message));
            }
            ++j;
        }

        return messageBuilder.build();
    }

    private LcdImageLine backgroundLine(int bitLine) {
        return extractLine(bitLine, ImageType.BACKGROUND)
                .mapColors(regs.get(Reg.BGP));
    }

    private LcdImageLine windowLine(int bitLine) {
        return extractLine(bitLine, ImageType.WINDOW);
    }

    private LcdImageLine extractLine(int bitLine, ImageType type) {
        Objects.checkIndex(bitLine, IMAGE_DIMENSION);

        int lineOfTheTile = bitLine / Byte.SIZE;

        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(
                IMAGE_DIMENSION);

        int slot;

        switch (type) {
        case BACKGROUND:
            slot = testLCDCBit(LCDCBit.BG_AREA) ? 1 : 0;
            break;
        case WINDOW:
            slot = testLCDCBit(LCDCBit.WIN_AREA) ? 1 : 0;
            break;
        default:
            throw new IllegalArgumentException(
                    "The type must be BACKGROUND or WINDOW");
        }

        for (int i = 0; i < IMAGE_DIMENSION / Byte.SIZE; ++i) {

            int tileIndexInRam = tileIndexInRam(i, lineOfTheTile)
                    + AddressMap.BG_DISPLAY_DATA[slot];

            int tileName = videoRam.read(tileIndexInRam);
            int lineInTheTile = bitLine % (OCTETS_INFOS_PER_TILE / 2);

            int lsb = getTileLineLsb(lineInTheTile, tileName);
            int msb = getTileLineMsb(lineInTheTile, tileName);

            lineBuilder.setBytes(i, msb, lsb);
        }

        return lineBuilder.build();
    }

    private int tileIndexInRam(int tileX, int tileY) {
        int nbTileInALine = IMAGE_DIMENSION / TILE_DIMENSION;
        Objects.checkIndex(tileX, nbTileInALine);
        Objects.checkIndex(tileY, nbTileInALine);

        return tileY * nbTileInALine + tileX;
    }

    private int getTileLineAddress(int line, int tileName, boolean isSprite,
            boolean useLcdBit, boolean tileSource) {
        Objects.checkIndex(line, TILE_DIMENSION * 2);
        Objects.checkIndex(tileName, TILES_CHOICES_PER_IMAGE);

        tileSource = useLcdBit ? testLCDCBit(LCDCBit.TILE_SOURCE) : tileSource;

        if (line >= 0 && line < TILE_DIMENSION) {
            int tileAddress;
            int tileInterval = TILES_CHOICES_PER_IMAGE / 2;

            if (tileName < tileInterval) {
                tileAddress = tileSource || isSprite ? AddressMap.TILE_SOURCE[1]
                        : (AddressMap.TILE_SOURCE[0]
                                + tileInterval * OCTETS_INFOS_PER_TILE);
            } else {
                tileAddress = AddressMap.TILE_SOURCE[0];
            }

            tileAddress += (tileName % tileInterval) * OCTETS_INFOS_PER_TILE
                    + line * 2;
            return tileAddress;
        } else {
            return getTileLineAddress(line - 8, tileName + 1, true, useLcdBit,
                    tileSource);
        }
    }

    private int getTileLineMsb(int lineInTheTile, int tileName) {
        return getTileLineMsb(lineInTheTile, tileName, false);
    }

    private int getTileLineMsb(int lineInTheTile, int tileName,
            boolean isSprite) {

        return getTileLineMsb(lineInTheTile, tileName, isSprite, true, true);
    }

    private int getTileLineMsb(int lineInTheTile, int tileName,
            boolean isSprite, boolean useLcdBit, boolean tileSource) {
        return Bits.reverse8(videoRam.read(getTileLineAddress(lineInTheTile,
                tileName, isSprite, useLcdBit, tileSource) + 1));
    }

    private int getTileLineLsb(int lineInTheTile, int tileName) {
        return getTileLineLsb(lineInTheTile, tileName, false);
    }

    private int getTileLineLsb(int lineInTheTile, int tileName,
            boolean isSprite) {
        return getTileLineLsb(lineInTheTile, tileName, isSprite, true, true);
    }

    private int getTileLineLsb(int lineInTheTile, int tileName,
            boolean isSprite, boolean useLcdBit, boolean tileSource) {
        return Bits.reverse8(videoRam.read(getTileLineAddress(lineInTheTile,
                tileName, isSprite, useLcdBit, tileSource)));
    }

    private int[] spritesIntersectingLine(int lcdLine) {
        Objects.checkIndex(lcdLine, LCD_HEIGHT);

        int[] sprites = new int[MAX_NUMBER_OF_SPRITES_PER_LINE];
        int j = 0;
        for (int index = 0; index < NUMBER_OF_SPRITES
                && j < MAX_NUMBER_OF_SPRITES_PER_LINE; index++) {
            int yCood = getAttribute(index, SpriteAttribute.Y) - Y_AXIS_DELAY;
            if (lcdLine >= yCood && lcdLine < yCood + TILE_DIMENSION
                    * (testLCDCBit(LCDCBit.OBJ_SIZE) ? 2 : 1)) {
                int xCood = getAttribute(index, SpriteAttribute.X);
                sprites[j] = Bits.make16(xCood, index);
                j++;
            }
        }
        Arrays.sort(sprites, 0, j);

        int[] finalSprites = Arrays.copyOf(sprites, j);

        for (int i = 0; i < finalSprites.length; i++) {
            finalSprites[i] = Bits.clip(Byte.SIZE, finalSprites[i]);
        }

        return finalSprites;
    }

    private int getAttribute(int spriteIndex, SpriteAttribute att) {
        Objects.checkIndex(spriteIndex, NUMBER_OF_SPRITES);

        int address = AddressMap.OAM_START
                + spriteIndex * NUMBER_OF_OCTETS_PER_SPRITE;
        switch (att) {
        case Y:
            return OAM.read(address);
        case X:
            return OAM.read(address + 1);
        case TILE:
            return OAM.read(address + 2);
        case SPECIAL:
            return OAM.read(address + 3);
        default:
            throw new Error();
        }
    }

    private boolean testSPECIALbit(int spriteIndex, SPECIALBit bit) {
        return Bits.test(getAttribute(spriteIndex, SpriteAttribute.SPECIAL),
                bit);
    }

    private int[] depthSprites(int[] allSprites, boolean bg) {
        int[] sprites = new int[MAX_NUMBER_OF_SPRITES_PER_LINE];

        int j = 0;
        for (int index : allSprites) {
            if (testSPECIALbit(index, SPECIALBit.BEHIND_BG) == bg) {
                sprites[j] = index;
                ++j;
            }
        }
        return Arrays.copyOf(sprites, j);
    }

    private LcdImageLine backGroundSprites(int bitLineInLcd, int[] allSprites) {
        return combinedSprites(bitLineInLcd, allSprites, true);
    }

    private LcdImageLine foreGroundSprites(int bitLineInLcd, int[] allSprites) {
        return combinedSprites(bitLineInLcd, allSprites, false);
    }

    private LcdImageLine combinedSprites(int bitLineInLcd, int[] allSprites,
            boolean bg) {
        Objects.checkIndex(bitLineInLcd, LCD_HEIGHT);

        int[] sprites = depthSprites(allSprites, bg);

        LcdImageLine combinedSprites = new LcdImageLine.Builder(LCD_WIDTH)
                .build();
        for (int sprite : sprites)
            combinedSprites = individualSprite(sprite, bitLineInLcd)
                    .below(combinedSprites);

        return combinedSprites;
    }

    private LcdImageLine individualSprite(int spriteIndex, int lineInLcd) {
        Objects.checkIndex(spriteIndex, NUMBER_OF_SPRITES);
        Objects.checkIndex(lineInLcd, LCD_HEIGHT);

        LcdImageLine.Builder b = new LcdImageLine.Builder(LCD_WIDTH);

        int lineInTheTile = lineInLcd
                - getAttribute(spriteIndex, SpriteAttribute.Y) + Y_AXIS_DELAY;

        if (lineInTheTile >= 0 && (lineInTheTile < TILE_DIMENSION
                || (testLCDCBit(LCDCBit.OBJ_SIZE)
                        && lineInTheTile < TILE_DIMENSION * 2))) {

            if (testSPECIALbit(spriteIndex, SPECIALBit.FLIP_V)) {
                lineInTheTile = (testLCDCBit(LCDCBit.OBJ_SIZE)
                        ? 2 * TILE_DIMENSION
                        : TILE_DIMENSION) - 1 - lineInTheTile;
            }

            int msb = getTileLineMsb(lineInTheTile,
                    getAttribute(spriteIndex, SpriteAttribute.TILE), true);
            int lsb = getTileLineLsb(lineInTheTile,
                    getAttribute(spriteIndex, SpriteAttribute.TILE), true);

            if (testSPECIALbit(spriteIndex, SPECIALBit.FLIP_H)) {
                msb = Bits.reverse8(msb);
                lsb = Bits.reverse8(lsb);
            }
            b.setBytes(0, msb, lsb);

            int palette = testSPECIALbit(spriteIndex, SPECIALBit.PALETTE)
                    ? regs.get(Reg.OBP1)
                    : regs.get(Reg.OBP0);

            return b.build().mapColors(palette)
                    .shift(LCD_WIDTH - TILE_DIMENSION)
                    .shift(-LCD_WIDTH + TILE_DIMENSION
                            + getAttribute(spriteIndex, SpriteAttribute.X)
                            - X_AXIS_DELAY);
        }

        return b.build();
    }

    private LcdImageLine computeMessageLine(int lineInText, String message) {
        Objects.checkIndex(lineInText, TILE_DIMENSION);

        LcdImageLine.Builder b = new LcdImageLine.Builder(
                message.length() * TILE_DIMENSION);
        for (int character = 0; character < message.length(); character++) {
            int indexOfTile = CharactereTiles.CHARACTERS_IN_ORDER
                    .indexOf(message.charAt(character));

            if (indexOfTile < 0)
                indexOfTile = CharactereTiles.CHARACTERS_IN_ORDER.length() - 1;

            int addresse = indexOfTile * TILE_DIMENSION + lineInText;

            int msbAndlsb = Bits.reverse8(charactersTiles.read(addresse));

            b.setBytes(character, msbAndlsb, msbAndlsb);
        }

        return b.build();
    }

    private String multiple32String(String s, int size) {
        Preconditions
                .checkArgument(size > 0 && size % 4 == 0 && size > s.length());

        StringBuilder s32 = new StringBuilder();

        s32.append(s);

        int sizeDiff = size - s.length();

        for (int i = 0; i < sizeDiff; i++) {
            s32.append(' ');
        }

        return s32.toString();
    }

    private boolean testLCDCBit(LCDCBit bit) {
        return regs.testBit(Reg.LCDC, bit);
    }
}
