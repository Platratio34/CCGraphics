package com.peter.ccgraphics.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.peter.ccgraphics.CCGraphics;

public class LuaFont {

    private static final char MISSING_CHAR = '\u0000';

    /** Name of the font */
    public final String name;
    /** If the font is mono-spaced */
    public final boolean isMono;

    /** Width of a character. If font is not monospaced use {@link #getWidth} instead */
    public final int charWidth;
    /** Height of a character */
    public final int charHeight;

    /** Map of characters glyphs. Glyph data is stored by row */
    private HashMap<Character, CharacterGlyph> chars = new HashMap<Character, CharacterGlyph>();

    /** Map of character data */
    private final HashMap<Character, CharData> charData = new HashMap<Character, CharData>();

    /** If the font is uppercase only */
    public final boolean upper;

    /** Name of character file */
    protected final String characterFile;
    private final int version;

    /*
     * <h4>Keys:</h4>
     * <ul>
     *  <li> <code>name</code> String- Font name </li>
     *  <li> <code>isMono</code> Boolean - <i>OPTIONAL.</i> If the font is monospaced. Defaults to <code>false</code> </li>
     *  <li> <code>charWidth</code> Integer - Width of a character. If font is not monospaced, represents default / space width </li>
     *  <li> <code>charHeight</code> Integer - Height of a character </li>
     *  <li> <code>upper</code> Boolean - <i>OPTIONAL.</i> If the font is uppercase only. Defaults to <code>false</code> </li>
     *  <li> <code>chars</code> JsonObject[] - Character data. Must contain a character for u0000 at index 0 </li>
     * </ul>
     * 
     * <h4>Character Data keys:</h4>
     * <ul>
     *  <li> <code>char</code> Char - Character this glyph is for </li>
     *  <li> <code>width</code> Integer - <i>OPTIONAL.</i> Width of the character. Defaults to <code>charWidth</code> </li>
     *  <li> <code>loc</code> Integer - <i>OPTIONAL.</i> Location of the character glyph in the font file. Defaults to the next available space </li>
     * </ul>
     */

    /**
     * Make a new font from the provided JSON object.
     * @see ??? for format specification
     * @param json JSON object to read font data from
     */
    public LuaFont(JsonObject json) {
        version = json.has("version") ? json.get("version").getAsInt() : 1;

        assertKey(json, "name");
        name = json.get("name").getAsString();

        if (json.has("isMono")) {
            isMono = json.get("isMono").getAsBoolean();
        } else {
            isMono = true;
        }

        upper = json.has("upper") ? json.get("upper").getAsBoolean() : false;

        assertKey(json, "charWidth");
        charWidth = json.get("charWidth").getAsInt();
        assertKey(json, "charHeight");
        charHeight = json.get("charHeight").getAsInt();

        assertKey(json, "characterFile");
        characterFile = json.get("characterFile").getAsString();
        boolean isBin = characterFile.endsWith(".bin");
        
        assertKey(json, "chars");
        JsonArray charWidthsJson = json.get("chars").getAsJsonArray();
        int nextLoc = 0;
        for (int i = 0; i < charWidthsJson.size(); i++) {
            JsonObject charJson = charWidthsJson.get(i).getAsJsonObject();
            assertKey(charJson, "char", "in glyph " + i);
            JsonElement cJson = charJson.get("char");
            char c;
            if(cJson.getAsJsonPrimitive().isString())
                c = charJson.get("char").getAsString().charAt(0);
            else
                c = (char) cJson.getAsInt();
            if (charData.containsKey(c))
                throw new RuntimeException("Repeated character: `" + c + "`: \\u" + ((int) c));
            if (isBin) {
                int w = isMono ? charWidth : (charJson.has("width") ? charJson.get("width").getAsInt() : charWidth);
                int loc = -1;
                if (charJson.has("loc")) {
                    loc = charJson.get("loc").getAsInt();
                } else {
                    loc = nextLoc;
                }
                int locSize = loc + (w * charHeight);
                nextLoc = (locSize > nextLoc) ? locSize : loc;
                if (c == MISSING_CHAR) {
                    if (w != charWidth)
                        throw new RuntimeException("Glyph for \u0000 must be the standard character width");
                    if (loc != 0)
                        throw new RuntimeException("Glyph for \u0000 must be located at byte 0");
                }
                charData.put(c, new CharData(c, w, charHeight, loc));
            } else {
                int w = getIntOrDefault(charJson, "width", charWidth);
                int h = getIntOrDefault(charJson, "height", charHeight);
                if (h != charHeight)
                    CCGraphics.LOGGER.info("{} was {} tall", c, h);
                assertKey(charJson, "x", "in glyph " + i + ", for .png file");
                int x = charJson.get("x").getAsInt();
                assertKey(charJson, "y", "in glyph " + i + ", for .png file");
                int y = charJson.get("y").getAsInt();
                
                charData.put(c, new CharData(c, w, h, x, y));
            }
        }
        if (!charData.containsKey(MISSING_CHAR)) {
            throw new RuntimeException("Font must have glyph for \u0000");
        }
    }

    private void assertKey(JsonObject json, String name) {
        if (json.has(name))
            return;
        throw new NoSuchElementException("JSON must contain key `" + name + "`");
    }

    private void assertKey(JsonObject json, String name, String extra) {
        if (json.has(name))
            return;
        throw new NoSuchElementException("JSON must contain key `" + name + "` " + extra);
    }

    private int getIntOrDefault(JsonObject json, String key, int defVal) {
        if (!json.has(key)) {
            return defVal;
        }
        return json.get(key).getAsInt();
    }
    
    /**
     * Load the characters from the provided input stream.
     * <br/><br/>
     * <b>DOES NOT CLOSE STREAM</b>
     * @param stream Input stream of glyphs.
     * @throws IOException In an I/O error occurs
     */
    protected void loadCharacters(InputStream stream) throws IOException {
        if (characterFile.endsWith(".bin")) {
            byte[] bytes = stream.readAllBytes();
            for (CharData data : charData.values()) {
                CharacterGlyph glyph = new CharacterGlyph(this, data);
                // int[] charArr = new int[data.width * charHeight];
                if (data.loc >= bytes.length)
                    throw new RuntimeException(
                            "Could not load character `" + data.c + "`. Glyph located outside file.");
                for (int i = 0; i < data.width * charHeight; i++) {
                    if (data.loc + i >= bytes.length)
                        throw new RuntimeException(
                                "Could not load character `" + data.c + "`. Glyph extended outside file.");
                    // System.out.println(i + ", " + data.loc+ ", " + bytes.length);
                    // charArr[i] = bytes[data.loc + i];
                    int x = i % data.width;
                    int y = i / data.width;
                    if(bytes[data.loc + i] != 0)
                        glyph.setPixel(x, y);
                }
                chars.put(data.c, glyph);
            }
        } else if (characterFile.endsWith(".png")) {
            BufferedImage image = ImageIO.read(stream);
            for (CharData data : charData.values()) {
                CharacterGlyph glyph = new CharacterGlyph(this, data);
                for (int x = 0; x < data.width; x++) {
                    for (int y = 0; y < data.height; y++) {
                        int col = image.getRGB(data.x + x, data.y + y);
                        if (col != 0xff000000)
                            glyph.setPixel(x, y);
                    }
                }
                chars.put(data.c, glyph);
            }
        }
    }

    /**
     * Get the width of the specified character in this font. If font is monospaced, return is <code>charWidth</code>.
     * <br/><br/>
     * If character was not found it this font, returns with of <code>u0000</code>
     * @param c Character to get the width of
     * @return Character width
     */
    public int getWidth(char c) {
        if (upper)
            c = ("" + c).toUpperCase().charAt(0);
        if (isMono)
            return charWidth;
        if (!charData.containsKey(c))
            return charData.get(MISSING_CHAR).width;
        return charData.get(c).width;
    }

    /**
     * Get the glyph for the specified character in this font.
     * <br/><br/>
     * If character was not found it this font, returns the glyph for <code>u0000</code>
     * @param c Character to get the glyph for
     * @return Character glyph
     */
    public CharacterGlyph getChar(char c) {
        if (upper)
            c = ("" + c).toUpperCase().charAt(0);
        if (!chars.containsKey(c)) {
            // CCGraphics.LOGGER.warn("Unknown missing glyph for character `{}`: \\u{}", c, (int)c);
            return chars.get(MISSING_CHAR).copy();
        }
        return chars.get(c).copy();
    }

    
    // protected record CharData(char c, int width, int loc) {
    // }
    /**
     * Character Data struct.
     */
    protected class CharData {

        public final char c;
        public final int width;
        public final int height;
        public final int loc;
        protected final int x;
        protected final int y;

        public CharData(char c, int width, int height, int loc) {
            this.c = c;
            this.width = width;
            this.height = height;
            this.loc = loc;
            this.x = -1;
            this.y = -1;
        }

        public CharData(char c, int width, int height, int x, int y) {
            this.c = c;
            this.width = width;
            this.height = height;
            this.loc = -1;
            this.x = x;
            this.y = y;
        }
    }
}
