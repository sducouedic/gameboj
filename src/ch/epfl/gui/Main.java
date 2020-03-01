package ch.epfl.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.component.lcd.lcdControl.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * The main programm of the simulation, a JavaFX application
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Main extends Application {

    private final static int ENLARGEMENT = 3;
    private final static int WIDTH = LcdController.LCD_WIDTH * ENLARGEMENT;
    private final static int HEIGHT = LcdController.LCD_HEIGHT * ENLARGEMENT;
    private final static int BORDER_SIZE = 50;

    private final static double[] SPEEDS = { 1, 2, 3, 0.5 };

    private static Color color;
    private int turbo;
    private long catchup;
    private List<String> messages;
    private String time;

    private static enum Settings {
        NAME, TURBO, STATS, COLOR, PRINT, TIME
    }

    /**
     * TODO
     * 
     * @author sophie
     *
     */
    public static enum Color {
        BLACKWHITE, WEIRD, BLUE, ORIGINAL
    }

    // KeyMaps
    private static final Map<KeyCode, Joypad.Key> keyCode = new HashMap<KeyCode, Joypad.Key>() {
        {
            put(KeyCode.RIGHT, Key.RIGHT);
            put(KeyCode.LEFT, Key.LEFT);
            put(KeyCode.UP, Key.UP);
            put(KeyCode.DOWN, Key.DOWN);
        }
    };

    private static final Map<String, Joypad.Key> keyString = new HashMap<String, Joypad.Key>() {
        {
            put("a", Key.A);
            put("b", Key.B);
            put(" ", Key.SELECT);
            put("s", Key.START);
        }
    };

    private static final Map<String, Settings> settingsKeys = new HashMap<String, Settings>() {
        {
            put("t", Settings.TURBO);
            put("d", Settings.STATS);
            put("p", Settings.PRINT);
            put("c", Settings.COLOR);
        }
    };

    private static final Map<Settings, String> settingsMessages = new HashMap<Settings, String>() {
        {
            put(Settings.NAME, "");
            put(Settings.TURBO, " T TURBO MODE : ");
            put(Settings.COLOR, " C COLOR : ");
            put(Settings.STATS, " D DISPLAY INFORMATION : ");
            put(Settings.PRINT, " P SCREENSHOT");
            put(Settings.TIME, " TIME  ");
        }
    };

    /**
     * The main method of the main class that launches the application. This
     * method is called with the name of the given ROM file when we run the
     * program
     * 
     * @param args
     *            : the given argument that has to be the name of the ROM file
     *            of the Gameboy program
     * 
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Implements the method start of Application. Exits if the parameter raw (a
     * list of Strings) of the stage is not of size 1. The methods creates and
     * runs a GameBoy which the cartridge is obtained from the given ROM file,
     * and updates periodically the image displayed on the screen
     * 
     * @param stage
     *            : the primary stage for the application
     * 
     * @throws IOException
     * 
     * @see javafx.application#start(Stage)
     */
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        // Create GameBoy
        if (getParameters().getRaw().size() != 1) {
            System.exit(1);
        }

        long startTime = System.nanoTime();
        catchup = 0;
        color = Color.BLACKWHITE;
        time = "00H00M00S";

        String fileName = getParameters().getRaw().get(0);

        File romFile = new File(fileName);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        // Creates GUI
        Image image = getNormalImage(gb);
        ImageView normalImageView = new ImageView();
        normalImageView.setFitWidth(WIDTH);
        normalImageView.setFitHeight(HEIGHT);
        normalImageView.setImage(image);

        Image image2 = getStatsImage(gb);
        ImageView statsImageView = new ImageView();
        statsImageView.setImage(image2);

        GridPane gridPane = new GridPane();
        gridPane.add(normalImageView, 0, 0);
        GridPane.setValignment(normalImageView, VPos.TOP);
        gridPane.add(statsImageView, 1, 0);
        gridPane.setHgap(BORDER_SIZE);
        gridPane.setStyle("-fx-background-color: darksalmon;"
                + "-fx-alignment: center;" + "-fx-grid-lines-visible: false");

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setTitle("Gameboj");
        stage.setFullScreen(true);
        stage.show();
        stage.requestFocus();

        messages = new ArrayList<>();
        messages.add(fileName.toUpperCase());
        messages.add(
                settingsMessages.get(Settings.TURBO) + " " + SPEEDS[turbo]);
        messages.add(settingsMessages.get(Settings.STATS)
                + gb.lcdController().getDisplayMode().toString().toUpperCase());
        messages.add(settingsMessages.get(Settings.COLOR)
                + color.toString().toUpperCase());
        messages.add(settingsMessages.get(Settings.PRINT));
        messages.add(settingsMessages.get(Settings.TIME) + time);

        gb.lcdController().setInformationsMessages(messages);

        // Update GameBoy
        AnimationTimer timer = new AnimationTimer() {

            /**
             * Implements the method handle of AnimationTimer, the timer of the
             * animation. The method simulates the progression of the Gameboy,
             * in function of the time, given in nanosecond.
             * 
             * @param currentNanoTime
             *            : the current time in nanosecond units
             * 
             * @see javafx.animation#handle(long)
             */
            @Override
            public void handle(long currentNanoTime) {
                long elapsedTime = (currentNanoTime - startTime);
                double elapsedSeconds = elapsedTime / 1e9;

                long cycle = (long) (elapsedSeconds
                        * GameBoy.NUMBER_OF_CYCLES_PER_SECOND) + catchup;

                double sec = elapsedSeconds;
                double min = sec / 60;
                sec = sec % 60;
                double h = min / 60;
                min = min % 60;

                Integer seconds = (int) sec;
                Integer minuts = (int) min;
                Integer hours = (int) h;

                time = hours.toString() + "H" + minuts.toString() + "M"
                        + seconds.toString() + "S";
                messages.set(Settings.TIME.ordinal(),
                        settingsMessages.get(Settings.TIME) + time);

                // manage the situation when the user press a key
                scene.setOnKeyPressed(e -> {

                    Key k = getJoypadKey(e);

                    if (k != null) {
                        gb.joypad().keyPressed(k);
                    } else {
                        
                        if(e.getCode() == KeyCode.ENTER)
                            gb.lcdController().returnPressed();
                        
                        Settings sK = settingsKeys.get(e.getText());

                        if (sK != null) {
                            String tmpMess = settingsMessages.get(sK);

                            switch (sK) {
                            case TURBO:
                                turbo = (turbo + 1) % SPEEDS.length;
                                messages.set(Settings.TURBO.ordinal(),
                                        tmpMess + SPEEDS[turbo]);
                                break;

                            case STATS:
                                gb.lcdController().switchDisplayMode();
                                messages.set(Settings.STATS.ordinal(),
                                        tmpMess + gb.lcdController()
                                                .getDisplayMode().toString()
                                                .toUpperCase());
                                break;

                            case PRINT:
                                captureImage(gb);
                                break;

                            case COLOR:
                                Color[] tmp = Color.values();
                                color = tmp[(color.ordinal() + 1) % tmp.length];
                                messages.set(Settings.COLOR.ordinal(), tmpMess
                                        + color.toString().toUpperCase());
                                break;

                            default:
                                break;
                            }

                        }
                    }
                });

                // manage the situation when the user release
                scene.setOnKeyReleased(e -> {
                    Key k = getJoypadKey(e);

                    if (k != null) {
                        gb.joypad().keyReleased(k);
                    }
                });

                normalImageView.setOnMousePressed(e -> {
                    double cX = e.getX();
                    double cY = e.getY();

                    int xInLCD = (int) (cX / ENLARGEMENT);
                    int yInLCD = (int) (cY / ENLARGEMENT);

                    gb.lcdController().clickOnScreen(xInLCD, yInLCD);

                });
                
                statsImageView.setOnMousePressed(e -> {
                    int cX = (int) (e.getX()/ENLARGEMENT);
                    int cY = (int) (e.getY()/ENLARGEMENT);
                    
                    gb.lcdController().clickOnStatsScreen(cX, cY);
                });

                if (turbo != 0) {
                    long currentCycle = gb.cycles();
                    long difference = cycle - currentCycle;
                    gb.runUntil(
                            currentCycle + (long) (difference * SPEEDS[turbo]));
                    catchup += (long) (difference * SPEEDS[turbo]) - difference;
                } else {
                    gb.runUntil(cycle);
                }

                updateNormalImage(normalImageView, gb);
                updateStatsImage(statsImageView, gb);
                gb.lcdController().setInformationsMessages(messages);

            }
        };

        timer.start();
    }

    private Key getJoypadKey(KeyEvent e) {
        Key k = null;

        k = keyCode.get(e.getCode());

        if (k == null) {
            k = keyString.get(e.getText());
        }

        return k;
    }

    private static final Image getNormalImage(GameBoy gb) {
        return ImageConverter.convert(gb.lcdController().currentImage(), color);
    }

    private static final Image getStatsImage(GameBoy gb) {
        return ImageConverter.convert(gb.lcdController().statsImage());
    }

    private void updateNormalImage(ImageView im, GameBoy gb) {
        im.setImage(null);
        im.setImage(getNormalImage(gb));
        im.setFitHeight(getNormalImage(gb).getHeight() * ENLARGEMENT);
        im.setFitWidth(getNormalImage(gb).getWidth() * ENLARGEMENT);
    }

    private void updateStatsImage(ImageView im, GameBoy gb) {
        im.setImage(null);
        Image tmp = getStatsImage(gb);
        if (tmp != null) {
            im.setFitWidth(tmp.getWidth() * ENLARGEMENT);
            im.setFitHeight(tmp.getHeight() * ENLARGEMENT);
            im.setImage(tmp);
        }
    }

    private void captureImage(GameBoy gb) {
        LcdImage li = gb.lcdController().currentImage();
        try {
            ImageIO.write(ImageConverter.convertBuff(li, color), "png",
                    new File(time + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
