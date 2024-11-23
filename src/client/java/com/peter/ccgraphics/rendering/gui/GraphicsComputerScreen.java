package com.peter.ccgraphics.rendering.gui;

import com.peter.ccgraphics.computer.GraphicsComputerMenu;

import dan200.computercraft.client.gui.AbstractComputerScreen;
import dan200.computercraft.client.gui.GuiSprites;
import dan200.computercraft.client.gui.widgets.ComputerSidebar;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.client.render.ComputerBorderRenderer;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.SpriteRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

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

    @Override
    protected void drawBackground(DrawContext graphics, float delta, int mouseX, int mouseY) {
        TerminalWidget terminal = this.getTerminal();
        SpriteRenderer spriteRenderer = SpriteRenderer.createForGui(graphics, RenderTypes.GUI_SPRITES);
        GuiSprites.ComputerTextures computerTextures = GuiSprites.getComputerTextures(this.family);
        ComputerBorderRenderer.render(spriteRenderer, computerTextures, terminal.getX(), terminal.getY(),
                terminal.getWidth(), terminal.getHeight(), false);
        ComputerSidebar.renderBackground(spriteRenderer, computerTextures, this.x, this.y + this.sidebarYOffset);
        graphics.draw();
    }

}
