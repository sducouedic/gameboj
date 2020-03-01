package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.component.lcd.lcdControl.LcdImageGenerator;

/**
 * A whole Image
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 * 
 */
public final class LcdImage {

    private static final int RIGHT_BORDER = 0b1000_0000;
    private static final int LEFT_BORDER = 1;

    private final List<LcdImageLine> lines;

    private final int width;
    private final int height;

    /**
     * Constructs an LcdImage of the given size (width, height), and containing
     * the given lines
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @param lines
     *            a list of LcdImageLine, the lines of the image
     * @throws IllegalArgumentException
     *             if the width is not a multiple of 32 or if it is less or
     *             equal to zero, or if the height is not strictely positive
     * @throws IllegalArgumentException
     *             if at least one line of lines is not of size width of if the
     *             list is not of size height
     */
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        Objects.requireNonNull(lines);
        Preconditions.checkArgument(width > 0 && width % 32 == 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkArgument(lines.size() == height);

        this.width = width;
        this.height = height;

        this.lines = new ArrayList<>();

        for (LcdImageLine line : lines) {
            Preconditions.checkArgument(line.size() == width);
            this.lines.add(line);
        }
    }

    /**
     * Getter for the width of the image
     * 
     * @return the width of the image
     */
    public int width() {
        return width;
    }

    /**
     * Getter for the height of the image
     * 
     * @return the height of the image
     */
    public int height() {
        return height;
    }

    /**
     * Returns the color of a pixel whose index is (x,y) in the form of an
     * integer between 0 and 3
     * 
     * @param x
     *            horizontal axis index
     * @param y
     *            vertical axis index
     * @return the pixel's color
     */
    public int get(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);

        int msb = lines.get(y).msb().testBit(x) ? 1 : 0;
        int lsb = lines.get(y).lsb().testBit(x) ? 1 : 0;

        return (msb << 1) + lsb;
    }

    /**
     * Creates an lcdImage from two images of same size by superposing them. The
     * resulting image's pixels are those of the above line, if these are
     * opaque, and those of the line below if they are not.
     * 
     * @param that
     *            the other image to be above the current image
     * @return the result image, with the given image superposed on the current
     *         image
     */
    public LcdImage below(LcdImage that) {
        Objects.requireNonNull(that);
        Preconditions
                .checkArgument(width == that.width && height == that.height);

        Builder b = new Builder(width, height);

        for (int line = 0; line < lines.size(); line++) {
            b.setLine(line, lines.get(line).below(that.lines.get(line)));
        }

        return b.build();
    }

    /**
     * Constructs and returns an image of (imageWidth x imageHeight) size, with
     * a empty rectangle inside. The thickness of the border of the rectangle is
     * 1 bit. The left top corner of rectangle is placed at the given position
     * (x,y), and is of the given size (rectangleWidth x rectangleHeight). The
     * image which contains the rectangle is toric : if rectangle exceeds on a
     * side, it reappears on the other side
     * 
     * @param imageWidth
     *            : the desired width of the returned LcdImage, has to be
     *            strictly positive and a multiple of 32
     * @param imageHeight
     *            : the desired height of the returned LcdImage, has to be
     *            strictly positive
     * @param x
     *            : the horizontal coordinate of the left top corner
     * @param y
     *            : the vertical coordinate of the left top corner
     * 
     * @param rectangleWidth
     *            : the width of the rectangle, has to be strictly positive and
     *            a multiple of 32, and has to be less or equal than imageWidth
     * 
     * @param rectangleHeight
     *            : the height of the rectangle, has to be strictly positive and
     *            has to be less or equal than imageHeight
     * 
     * @return the resulted LcdImage with the rectangle inside
     * 
     * @throws IllegalArgumentException
     *             if the width of the rectangle or of the image is not a
     *             multiple of 32 or if it is less or equal to zero, or if the
     *             height of the rectangle or of the image is not strictly
     *             positive
     * @throws IllegalArgumentException
     *             if the dimension of the image is smaller of the dimensions of
     *             the rectangle
     */
    public static LcdImage createRectangleBorder(int imageWidth,
            int imageHeight, int x, int y, int rectangleWidth,
            int rectangleHeight) {

        Preconditions.checkArgument(
                imageWidth > 0 && imageWidth % 32 == 0 && imageHeight > 0);
        Preconditions.checkArgument(rectangleWidth > 0
                && rectangleWidth % 32 == 0 && rectangleWidth > 0);
        Preconditions.checkArgument(
                imageWidth >= rectangleWidth && imageHeight >= rectangleHeight);

        int yStart = (y < 0 || y > imageHeight) ? Math.floorMod(y, imageHeight)
                : y;
        int yEnd = Math.floorMod(yStart + rectangleHeight, imageHeight);

        Builder imB = new Builder(imageWidth, imageHeight);

        for (int l = 0; l < imageHeight; l++) {

            BitVector zeros = new BitVector(imageWidth, false);
            LcdImageLine line = new LcdImageLine(zeros, zeros, zeros);

            if (l == yStart || l == yEnd) {
                BitVector hBorder = new BitVector(rectangleWidth, true)
                        .extractZeroExtended(0, imageWidth);
                line = new LcdImageLine(hBorder, hBorder, hBorder);
            } else {
                LcdImageLine.Builder lB = new LcdImageLine.Builder(imageWidth);

                if ((yStart < yEnd && l > yStart && l < yEnd)
                        || (yStart > yEnd && (l > yStart || l < yEnd))) {
                    lB.setBytes(0, LEFT_BORDER, LEFT_BORDER);
                    lB.setBytes(rectangleWidth / Byte.SIZE - 1, RIGHT_BORDER,
                            RIGHT_BORDER);
                }
                line = lB.build();
            }

            imB.setLine(l, line.extractWrapped(-x, imageWidth));
        }

        return imB.build();
    }

//    /**
//     * Constructs and returns an LcdImage with two LcdImages binded, one on the
//     * top of the other. The two images have to be on the same width
//     * 
//     * @param top
//     *            : the image to be placed on the top
//     * @param bottom
//     *            : the image to be on the bottom
//     * 
//     * @return the result image, that is a concatenation of the two givent
//     *         images
//     * @throws IllegalArgument
//     *             exception : if the width is not the same for the two images
//     */
//    public static LcdImage bindVertical(LcdImage top, LcdImage bottom) {
//        Objects.requireNonNull(top);
//        Objects.requireNonNull(bottom);
//        Preconditions.checkArgument(top.width == bottom.width);
//
//        ArrayList<LcdImageLine> l = new ArrayList<>();
//        l.addAll(top.lines);
//        l.addAll(bottom.lines);
//
//        return new LcdImage(top.width, top.height + bottom.height, l);
//    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        Objects.requireNonNull(that);
        Preconditions.checkArgument(that instanceof LcdImage);

        LcdImage tmp = (LcdImage) that;
        if (width() != tmp.width || height != tmp.height) {
            return false;
        }

        for (int i = 0; i < height; ++i) {
            if (!lines.get(i).equals(tmp.lines.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Builder pattern
     * 
     * @author Arnaud Robert (287964)
     * @author Sophie Du Couedic (260007)
     * 
     */
    public static final class Builder {

        private List<LcdImageLine> lines;
        private int height;
        private int width;

        /**
         * Creates an LcdImage builder
         * 
         * @param width
         *            the width of the image
         * @param height
         *            the height of the image
         * @throws IndexOutOfBoundsException
         *             if width or height are less than or equal to 0
         */
        public Builder(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IndexOutOfBoundsException();
            }

            this.width = width;
            this.height = height;
            lines = new ArrayList<>();

            int i = 0;
            while (i < height) {
                LcdImageLine.Builder b = new LcdImageLine.Builder(width);
                lines.add(b.build());
                i++;
            }
        }

        /**
         * Changes the line at the given index by replacing it by the line given
         * as parameter. This method can not be used if the builder has already
         * built an image
         * 
         * @param index
         *            the index of the line to change
         * @param newLine
         *            the line's replacement
         * @throws IllegalStateException
         *             if the builder has already built an image
         * @return the current instance of the builder
         */
        public Builder setLine(int index, LcdImageLine newLine) {
            Objects.requireNonNull(newLine);
            checkIfBuiltAlready();
            Objects.checkIndex(index, height);

            Preconditions.checkArgument(newLine.size() == width);
            lines.set(index, newLine);
            return this;
        }

        /**
         * Builds the LcdImage
         * 
         * @throws IllegalStateException
         *             if the builder has already built an image
         * @return the LcdImage whose construction is now finished
         */
        public LcdImage build() {
            checkIfBuiltAlready();
            LcdImage result = new LcdImage(width, height, lines);
            lines = null;
            return result;
        }

        private void checkIfBuiltAlready() {
            if (lines == null) {
                throw new IllegalStateException();
            }
        }
    }
}
