package com.polantic.totemlimit;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public final class Totemlimit implements ModInitializer {
    private static final Item TOTEM = Items.TOTEM_OF_UNDYING;
    private static final Text WARN = Text.literal("You cannot have more than one totem in your inventory.")
            .formatted(Formatting.RED);

    @Override
    public void onInitialize() {
    }

    public static boolean isTotem(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(TOTEM);
    }

    public static void warn(ServerPlayerEntity player) {
        player.sendMessage(WARN, true);
    }

    public static boolean hasAnyTotem(ServerPlayerEntity player) {
        return hasAnyTotem(player, true);
    }

    public static boolean hasAnyTotem(ServerPlayerEntity player, boolean includeCursor) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (isTotem(inv.getStack(i))) return true;
        }
        if (isTotem(player.getOffHandStack())) return true;
        if (includeCursor && player.currentScreenHandler != null) {
            if (isTotem(player.currentScreenHandler.getCursorStack())) return true;
        }
        return false;
    }

    public static boolean hasTotemExcluding(ServerPlayerEntity player, ItemStack exclude, boolean includeCursor) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s == exclude) continue;
            if (isTotem(s)) return true;
        }
        ItemStack offhand = player.getOffHandStack();
        if (offhand != exclude && isTotem(offhand)) return true;
        if (includeCursor && player.currentScreenHandler != null) {
            ItemStack cursor = player.currentScreenHandler.getCursorStack();
            if (cursor != exclude && isTotem(cursor)) return true;
        }
        return false;
    }

    public static void enforceSingleTotem(ServerPlayerEntity player) {
        if (player.isCreative()) return;

        boolean keepFound = false;
        boolean droppedAny = false;

        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!isTotem(stack)) continue;

            if (!keepFound) {
                keepFound = true;
                continue;
            }

            ItemStack toDrop = stack.copy();
            inv.removeStack(i);
            player.dropItem(toDrop, true, false);
            droppedAny = true;
        }

        if (player.currentScreenHandler != null) {
            ItemStack cursor = player.currentScreenHandler.getCursorStack();
            if (isTotem(cursor)) {
                if (!keepFound) {
                    keepFound = true;
                } else {
                    ItemStack toDrop = cursor.copy();
                    player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    player.dropItem(toDrop, true, false);
                    droppedAny = true;
                }
            }
        }

        if (droppedAny) {
            warn(player);
        }
    }
}