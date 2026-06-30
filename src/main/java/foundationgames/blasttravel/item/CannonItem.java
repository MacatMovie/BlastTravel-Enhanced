package foundationgames.blasttravel.item;

import foundationgames.blasttravel.BlastTravel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CannonItem extends Item {
    public CannonItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(controlLine("", "[Right-click]", " a placed cannon to enter."));
            tooltip.add(controlLine("Press ", "[Spacebar]", " to shoot."));
            tooltip.add(controlLine("Press ", "[Shift]", " while inside the cannon to exit normally."));
            tooltip.add(controlLine("", "[Shift + Right-click]", " a placed cannon to open its UI."));
        } else {
            tooltip.add(Component.empty()
                    .append(Component.literal("Hold ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("[Shift]").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" for Summary").withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    private static Component controlLine(String before, String key, String after) {
        return Component.empty()
                .append(Component.literal(before).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(key).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(after).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel world) {
            var pos = context.getClickedPos().relative(context.getClickedFace());
            var cannon = BlastTravel.CANNON.get().create(world);
            if (cannon != null) {
                cannon.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0);
                world.addFreshEntity(cannon);
                if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                }
            }
        }

        context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 0.8F);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
