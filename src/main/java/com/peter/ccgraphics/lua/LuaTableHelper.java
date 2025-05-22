package com.peter.ccgraphics.lua;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Collection of helper functions for interacting with LUA tables in Java code.
 */
public class LuaTableHelper {

    /**
     * Check if the provided table has a key with a value of the given type
     * 
     * @param map Table to check in
     * @param key Key to check for
     * @param c   Class of the value to check for
     * @return If a key with typed value was present
     */
    public static boolean hasOfType(Map<?, ?> map, Object key, Class<?> c) {
        if (!map.containsKey(key))
            return false;
        Object o = map.get(key);
        return c.isInstance(o);
    }

    /**
     * Checks if the table contains a key with a numeric value
     * 
     * @param map Table to check in
     * @param key Key to check
     * @return If the key was present, and the value was numeric (including
     *         convertible from string)
     */
    public static boolean hasNumber(Map<?, ?> map, Object key) {
        if (!map.containsKey(key))
            return false;
        Object o = map.get(key);
        if (o instanceof String str) {
            try {
                Double.valueOf(str);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return o instanceof Double || o instanceof Integer;
    }

    /**
     * Get int value from the table. Will try to convert strings to numbers
     * 
     * @param map Table to get from
     * @param key Key to get
     * @return int value at the key, including conversion from string.
     * @throws NoSuchElementException If the key could not be found, or if the value
     *                                could not be made numeric
     */
    public static int getInt(Map<?, ?> map, Object key) {
        if (!map.containsKey(key))
            throw new NoSuchElementException("No entry with key " + key + " was found in the table");
        Object o = map.get(key);
        if (o instanceof Integer)
            return (int) o;
        if (o instanceof Double)
            return (int) (double) o;
        if (o instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {

            }
            try {
                return (int) Double.parseDouble(str);
            } catch (NumberFormatException e) {

            }
        }
        throw new NoSuchElementException("No numeric entry with key " + key + " was found in the table");
    }

    /**
     * Get int value from the table. Will try to convert strings to numbers
     * 
     * @param map Table to get from
     * @param key Key to get
     * @param def Value to return if key is not present
     * @return int value at the key, including conversion from string.
     * @throws NoSuchElementException If the value could not be made numeric
     */
    public static int getIntOpt(Map<?, ?> map, Object key, int def) {
        if (!map.containsKey(key))
            return def;
        Object o = map.get(key);
        if (o instanceof Integer)
            return (int) o;
        if (o instanceof Double)
            return (int) (double) o;
        if (o instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {

            }
            try {
                return (int) Double.parseDouble(str);
            } catch (NumberFormatException e) {

            }
        }
        throw new NoSuchElementException("No numeric entry with key " + key + " was found in the table");
    }
}
