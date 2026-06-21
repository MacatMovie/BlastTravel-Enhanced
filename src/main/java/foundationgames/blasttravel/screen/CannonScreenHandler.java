package foundationgames.blasttravel.screen;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public class CannonScreenHandler extends AbstractContainerMenu {
    public final Container inventory;

    public CannonScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(3));
    }

    public CannonScreenHandler(int syncId, Inventory playerInv, Container inv) {
        super(BlastTravel.CANNON_SCREEN_HANDLER.get(), syncId);

        checkContainerSize(inv, 3);
        this.inventory = inv;
        inv.startOpen(playerInv.player);

        this.addSlot(new FilterSlot(inv, 0, 62, 20, stack -> stack.is(Items.GUNPOWDER)));
        this.addSlot(new FilterSlot(inv, 1, 80, 20, stack -> stack.is(Items.CHAIN)));
        this.addSlot(new FilterSlot(inv, 2, 98, 20, CannonBehavior::isValidBehaviorStack));

        int row;
        int col;
        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 58 + row * 18));
            }
        }
        for (row = 0; row < 9; ++row) {
            this.addSlot(new Slot(playerInv, row, 8 + row * 18, 116));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int fromSlotId) {
        var newStack = ItemStack.EMPTY;
        var fromSlot = this.slots.get(fromSlotId);

        if (fromSlot.hasItem()) {
            var fromStack = fromSlot.getItem();
            newStack = fromStack.copy();
            if (fromSlotId >= 0 && fromSlotId < 3) {
                if (!this.moveItemStackTo(fromStack, 3, 39, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(fromStack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if (fromStack.isEmpty()) fromSlot.set(ItemStack.EMPTY);
            else fromSlot.setChanged();
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    public static class FilterSlot extends Slot {
        private final Predicate<ItemStack> filter;

        public FilterSlot(Container inventory, int id, int x, int y, Predicate<ItemStack> filter) {
            super(inventory, id, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && filter.test(stack);
        }
    }
}
