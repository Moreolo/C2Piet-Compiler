package design;

public class PietColor {

    // Farben
    // Rot
    public static int red = (255 << 16) | (0 << 8) | 0;
    public static int lightRed = (255 << 16) | (192 << 8) | 192;
    public static int darkRed = (192 << 16) | (0 << 8) | 0;

    // Gelb
    public static int yellow = (255 << 16) | (255 << 8) | 0;
    public static int lightYellow = (255 << 16) | (255 << 8) | 192;
    public static int darkYellow = (192 << 16) | (192 << 8) | 0;

    // Grün
    public static int green = (0 << 16) | (255 << 8) | 0;
    public static int lightGreen = (192 << 16) | (255 << 8) | 192;
    public static int darkGreen = (0 << 16) | (192 << 8) | 0;

    // Cyan
    public static int cyan = (0 << 16) | (255 << 8) | 255;
    public static int lightCyan = (192 << 16) | (255 << 8) | 255;
    public static int darkCyan = (0 << 16) | (192 << 8) | 192;

    // Blau
    public static int blue = (0 << 16) | (0 << 8) | 255;
    public static int lightBlue = (192 << 16) | (192 << 8) | 255;
    public static int darkBlue = (0 << 16) | (0 << 8) | 192;

    // Magenta
    public static int magenta = (255 << 16) | (0 << 8) | 255;
    public static int lightMagenta = (255 << 16) | (192 << 8) | 255;
    public static int darkMagenta = (192 << 16) | (0 << 8) | 192;

    // Weiß
    public static int white = (255 << 16) | (255 << 8) | 255;

    // Schwarz
    public static int black = (0 << 16) | (0 << 8) | 0;

    // verschachteltes FarbenArray
    public static int[][] matrixOfColor = {
            { lightRed, red, darkRed },
            { lightYellow, yellow, darkYellow },
            { lightGreen, green, darkGreen },
            { lightCyan, cyan, darkCyan },
            { lightBlue, blue, darkBlue },
            { lightMagenta, magenta, darkMagenta },
    };

    public int hue;
    public int shade;

    public PietColor(int hue, int shade) {
        this.hue = hue;
        this.shade = shade;
    }

    public PietColor(boolean white, boolean black) {
        if(white)
            setWhite();
        else if(black)
            setBlack();
    }

    public void set(int hue, int shade) {
        this.hue = hue;
        this.shade = shade;
    }

    public void setWhite() {
        this.hue = -1;
        this.shade = 0;
    }

    public void setBlack() {
        this.hue = 0;
        this.shade = -1;
    }

    public int get() {
        if(hue == -1)
            return white;
        else if(shade == -1)
            return black;
        else
            return matrixOfColor[hue][shade];
    }

    public PietColor getCopy() {
        return new PietColor(this.hue, this.shade);
    }

    // Passt die aktuelle Farbe an, sodass eine Piet Operation leicht in die
    // passende Farbe übersetzt werden kann
    public void add(int hue, int shade) {
        // Ein Wert wird addiert und es wird der passende Modulo genommen (Die Farben
        // der Operationen sind zyklisch)
        this.hue = (this.hue + hue) % 6;
        this.shade = (this.shade + shade) % 3;
    }
}
