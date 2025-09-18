package com.peter.ccgraphics.rendering.gui;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.computer.GraphicsComputerMenu;

import dan200.computercraft.client.gui.AbstractComputerScreen;
import dan200.computercraft.client.gui.GuiSprites;
import dan200.computercraft.client.gui.GuiSprites.ComputerTextures;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.core.util.Nullability;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GraphicsComputerScreen extends AbstractComputerScreen<GraphicsComputerMenu> {

    public GraphicsComputerScreen(GraphicsComputerMenu container, PlayerInventory player, Text title) {
        super(container, player, title, 12);
        this.backgroundWidth = TerminalWidget.getWidth(this.terminalData.getWidth()) + 24 + 17;
        this.backgroundHeight = TerminalWidget.getHeight(this.terminalData.getHeight()) + 24;
    }

    @Override
    protected TerminalWidget createTerminal() {
        return new GraphicsScreenWidget(this.handler, input, this.x + 17 + 12, this.y + 12);
    }

    private static final Identifier GRAPHICS_BORDER = CCGraphics.id("gui/border_graphics");
    private static final Identifier GRAPHICS_SIDEBAR = CCGraphics.id("gui/sidebar_graphics");

    @Override
    protected void drawBackground(DrawContext graphics, float delta, int mouseX, int mouseY) {
        TerminalWidget terminal = this.getTerminal();
        graphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, GRAPHICS_BORDER, terminal.getX() - 12, terminal.getY() - 12, terminal.getWidth() + 24, terminal.getHeight() + 24);
        graphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, GRAPHICS_SIDEBAR, this.x, this.y + this.sidebarYOffset, 17, 38);
    }

}
