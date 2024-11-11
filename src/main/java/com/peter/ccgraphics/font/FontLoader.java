package com.peter.ccgraphics.font;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.peter.ccgraphics.CCGraphics;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class FontLoader implements SimpleSynchronousResourceReloadListener {

    public static final Identifier RESOURCE_ID = CCGraphics.id("fonts");
    public static final String FOLDER_NAME = "fonts";

    public static final HashMap<String, LuaFont> fonts = new HashMap<String, LuaFont>();

    @Override
    public Identifier getFabricId() {
        return RESOURCE_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        for (Identifier id : manager.findResources(FOLDER_NAME, path -> path.toString().endsWith(".json") ).keySet()) {
            try (BufferedReader reader = manager.getResource(id).get().getReader()) {
                JsonObject obj = JsonHelper.deserialize(reader);
                reader.close();
                LuaFont font = new LuaFont(obj);
                Identifier charId = Identifier.of(id.getNamespace(), FOLDER_NAME+"/"+ font.characterFile);
                Optional<Resource> fontFile = manager.getResource(charId);
                if (!fontFile.isPresent()) {
                    CCGraphics.LOGGER.error("Error loading font {}: Could not find font file {}", id, charId);
                    continue;
                }
                
                try (InputStream stream = fontFile.get().getInputStream()) {
                    font.loadCharacters(stream);
                } catch (Exception e) {
                    CCGraphics.LOGGER.error("Error loading font {}: {}", id, e.getMessage());
                    CCGraphics.LOGGER.error("", e);
                    continue;
                }
                fonts.put(font.name+"_"+font.charHeight, font);
                CCGraphics.LOGGER.info("Loaded font {}", id);
            } catch (Exception e) {
                CCGraphics.LOGGER.error("Error loading font: {}", e.getMessage());
                CCGraphics.LOGGER.error("", e);
                continue;
            }
        }
    }

    public static boolean hasFont(String name, int size) {
        return fonts.containsKey(name + "_" + size);
    }

    public static LuaFont getFont(String name, int size) {
        return fonts.get(name + "_" + size);
    }

}
