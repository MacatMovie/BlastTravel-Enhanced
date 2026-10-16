package foundationgames.blasttravel.client.screen;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class CannonScreen extends AbstractContainerScreen<CannonScreenHandler> {
    private static final ResourceLocation TEXTURE = BlastTravel.id("textures/gui/container/cannon.png");

    private final ItemStack[] allowedBehaviorStacks = CannonBehavior.allBehaviors().stream()
            .filter(behavior -> behavior.icon != Items.AIR)
            .map(behavior -> new ItemStack(behavior.icon))
            .toArray(ItemStack[]::new);

    private long time = (long) (Math.random() * 1000);

    public CannonScreen(CannonScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 140;
        this.inventoryLabelY = 47;
        this.titleLabelY = 8;
        this.titleLabelX = 61;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }


    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.index >= 0 && this.hoveredSlot.index < 3
                && (!this.hoveredSlot.hasItem() || Screen.hasShiftDown())) {
            graphics.renderComponentTooltip(this.font, slotTooltip(this.hoveredSlot.index), mouseX, mouseY);
        } else {
            super.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private static List<Component> slotTooltip(int slot) {
        return switch (slot) {
            case 0 -> List.of(
                    Component.translatable("tooltip.blasttravel.cannon.slot1.title").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.blasttravel.cannon.slot1.detail").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.blasttravel.cannon.slot1.detail2").withStyle(ChatFormatting.GRAY));
            case 1 -> List.of(
                    Component.translatable("tooltip.blasttravel.cannon.slot2.title").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.blasttravel.cannon.slot2.detail").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.blasttravel.cannon.slot2.detail2").withStyle(ChatFormatting.GRAY));
            case 2 -> List.of(
                    Component.translatable("tooltip.blasttravel.cannon.slot3.title").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.blasttravel.cannon.slot3.detail").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.blasttravel.cannon.slot3.detail2").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.blasttravel.cannon.slot3.detail3").withStyle(ChatFormatting.GRAY));
            default -> List.of();
        };
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.inventory.getItem(2).isEmpty() && this.allowedBehaviorStacks.length > 0) {
            int x = this.leftPos + 98;
            int y = this.topPos + 20;
            graphics.renderItem(this.allowedBehaviorStacks[Mth.floor((float) this.time / 30) % this.allowedBehaviorStacks.length], x, y);
            graphics.fill(x, y, x + 16, y + 16, 0x8B8B8B8B);
        }

        for (int i = 0; i < 2; i++) {
            if (this.menu.inventory.getItem(i).isEmpty()) {
                graphics.blit(TEXTURE, this.leftPos + 62 + (18 * i), this.topPos + 20, 16 * i, 140, 16, 16);
            }
        }
    }

    @Override
    protected void containerTick() {
        this.time++;
    }
}
