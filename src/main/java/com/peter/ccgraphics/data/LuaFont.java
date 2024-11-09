package com.peter.ccgraphics.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    private HashMap<Character, int[]> chars = new HashMap<Character, int[]>();

    /** Map of character data */
    private final HashMap<Character, CharData> charData = new HashMap<Character, CharData>();

    /** If the font is uppercase only */
    public final boolean upper;

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
                c = (char)cJson.getAsInt();
            int w = isMono ? charWidth : (charJson.has("width") ? charJson.get("width").getAsInt() : charWidth);
            int loc = -1;
            if (charJson.has("loc")) {
                loc = charJson.get("loc").getAsInt();
            } else {
                loc = nextLoc;
            }
            int locSize = loc + (w * charHeight);
            nextLoc = (locSize > nextLoc) ? locSize : loc;
            System.out.println(c + " " + Integer.toHexString(loc));
            if (c == MISSING_CHAR) {
                if (w != charWidth)
                    throw new RuntimeException("Glyph for \u0000 must be the standard character width");
                if (loc != 0)
                    throw new RuntimeException("Glyph for \u0000 must be located at byte 0");
            }
            charData.put(c, new CharData(c, w, loc));
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
    
    /**
     * Load the characters from the provided input stream.
     * <br/><br/>
     * <b>DOES NOT CLOSE STREAM</b>
     * @param stream Input stream of glyphs.
     * @throws IOException In an I/O error occurs
     */
    protected void loadCharters(InputStream stream) throws IOException {
        byte[] bytes = stream.readAllBytes();
        for (CharData data : charData.values()) {
            int[] charArr = new int[data.width * charHeight];
            for (int i = 0; i < charArr.length; i++) {
                charArr[i] = bytes[data.loc + i];
            }
            chars.put(data.c, charArr);
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
    public int[] getChar(char c) {
        if (upper)
            c = ("" + c).toUpperCase().charAt(0);
        if (!chars.containsKey(c)) {
            return chars.get(MISSING_CHAR).clone();
        }
        return chars.get(c).clone();
    }

    /**
     * Character Data record.
     */
    protected record CharData(char c, int width, int loc) {
    }
}
