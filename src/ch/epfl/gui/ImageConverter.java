package ch.epfl.gui;

import java.awt.image.BufferedImage;

import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gui.Main.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A class used to convert an LcdImage to his equivalent javafx Image
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class ImageConverter {

    private static final int[] BLACKWHITE = new int[] { 0xFF_FF_FF_FF,
            0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 };

    private static final int[] WEIRD = new int[] { 0xFF_FF_00_FF,
            0xFF_00_FF_00, 0xFF_00_A9_D3, 0xFF_00_43_00 };

    private static final int[] BLUE = new int[] { 0xFF_FF_FF_FF, 0xFF_23_E3_C3,
            0xFF_B9_2E_A1, 0xFF_00_00_99 };

    private static final int[] ORIGINAL = new int[] { 0xFF_9B_BC_0F,
            0xFF_8B_AC_0F, 0xFF_30_62_30, 0xFF_0F_38_0F };

    private static final int[][] COLOR_MAP_MAP = new int[][] { BLACKWHITE,
            WEIRD, BLUE, ORIGINAL };

    /**
     * The method used to convert an LcdImage to his equivalent javafx Image
     * 
     * @param image
     *            the image to convert
     * @return an image of type javafx.scene.image.Image
     */
    public static Image convert(LcdImage image, Color color) {

        if (image == null) {
            return null;
        }

        int width = image.width();
        int height = image.height();

        WritableImage wi = new WritableImage(width, height);

        PixelWriter writer = wi.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                writer.setArgb(i, j, COLOR_MAP_MAP[color.ordinal()][image.get(i, j)]);
            }
        }

        return wi;
    }

    public static Image convert(LcdImage image) {
        return convert(image, Color.BLACKWHITE);
    }

    public static BufferedImage convertBuff(LcdImage li, Color color) {
        BufferedImage i = new BufferedImage(li.width(), li.height(),
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < li.height(); ++y)
            for (int x = 0; x < li.width(); ++x)
                i.setRGB(x, y, COLOR_MAP_MAP[color.ordinal()][li.get(x, y)]);
        return i;
    }
}
