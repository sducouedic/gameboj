package ch.epfl.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public class Run {
    @Test
    void run() throws IOException {
        // Main.main(new String[] { "finalflappyboy.gb"});
        // Main.main(new String[] { "Bomberman.gb"});
         Main.main(new String[] { "Super_Mario_Land.gb"});
        // Main.main(new String[] { "The Legend of Zelda.gb"});
        // Main.main(new String[] { "Donkey_Kong.gb" });
        // Main.main(new String[] { "Super_Mario_Land_2.gb"});
        // Main.main(new String[] {"Tetris.gb"});
    }
}
