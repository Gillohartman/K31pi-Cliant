package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Comparator;

/** Sorts the player inventory (and chests), merges stacks, refills the hotbar. */
public final class InventorySorter {
	private static Method clickMethod;
	private static boolean clickFailed;

	private InventorySorter() {}

	private static final Comparator<ItemStack> ORDER = (a, b) -> {
		if (a.isEmpty() && b.isEmpty()) return 0;
		if (a.isEmpty()) return 1;
		if (b.isEmpty()) return -1;
		int c = key(a).compareTo(key(b));
		return c != 0 ? c : Integer.compare(b.getCount(), a.getCount());
	};

	private static String key(ItemStack s) {
		return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
	}

	public static void sort() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		MultiPlayerGameMode gm = mc.gameMode;
		if (player == null || gm == null) return;
		AbstractContainerMenu menu = player.containerMenu;
		if (!menu.getCarried().isEmpty()) {
			player.displayClientMessage(Component.literal("Erst den Gegenstand an der Maus ablegen"), true);
			return;
		}

		clickFailed = false;
		int id = menu.containerId;
		int size = menu.slots.size();

		if (menu instanceof InventoryMenu) {
			sortRange(gm, player, id, 9, 35);
		} else if (size >= 36) {
			sortRange(gm, player, id, size - 36, size - 10);
			if (Modules.SORT_CHEST.value && size > 36 && (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu)) {
				sortRange(gm, player, id, 0, size - 37);
			}
		}

		player.displayClientMessage(Component.literal(clickFailed ? "Sortieren fehlgeschlagen (Klick-Methode fehlt)" : "Sortiert"), true);
	}

	private static void sortRange(MultiPlayerGameMode gm, LocalPlayer p, int id, int first, int last) {
		if (Modules.SORT_MERGE.value) {
			for (int i = first; i <= last; i++) {
				for (int j = i + 1; j <= last; j++) {
					ItemStack a = slot(p, i);
					ItemStack b = slot(p, j);
					if (a.isEmpty() || b.isEmpty()) continue;
					if (!ItemStack.isSameItemSameComponents(a, b)) continue;
					if (a.getCount() >= a.getMaxStackSize()) break;
					click(gm, id, j, p);
					click(gm, id, i, p);
					if (!p.containerMenu.getCarried().isEmpty()) click(gm, id, j, p);
				}
			}
		}

		for (int i = first; i <= last; i++) {
			int best = i;
			for (int j = i + 1; j <= last; j++) {
				if (ORDER.compare(slot(p, j), slot(p, best)) < 0) best = j;
			}
			if (best != i && ORDER.compare(slot(p, best), slot(p, i)) != 0) {
				click(gm, id, best, p);
				click(gm, id, i, p);
				if (!p.containerMenu.getCarried().isEmpty()) click(gm, id, best, p);
			}
		}
	}

	// ---------------- hotbar refill ----------------
	private static ItemStack lastHeld = ItemStack.EMPTY;
	private static int lastSlot = -1;

	public static void refillTick(Minecraft mc) {
		LocalPlayer p = mc.player;
		MultiPlayerGameMode gm = mc.gameMode;
		if (p == null || gm == null || mc.screen != null) {
			lastHeld = ItemStack.EMPTY;
			lastSlot = -1;
			return;
		}
		int sel = p.getInventory().getSelectedSlot();
		ItemStack cur = p.getInventory().getItem(sel);

		if (sel == lastSlot && cur.isEmpty() && !lastHeld.isEmpty()) {
			clickFailed = false;
			for (int i = 9; i <= 35; i++) {
				ItemStack s = slot(p, i);
				if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, lastHeld)) {
					int id = p.inventoryMenu.containerId;
					click(gm, id, i, p);
					click(gm, id, 36 + sel, p);
					if (!p.containerMenu.getCarried().isEmpty()) click(gm, id, i, p);
					break;
				}
			}
			lastHeld = ItemStack.EMPTY;
			return;
		}
		lastSlot = sel;
		lastHeld = cur.isEmpty() ? ItemStack.EMPTY : cur.copy();
	}

	private static ItemStack slot(LocalPlayer p, int index) {
		return p.containerMenu.slots.get(index).getItem();
	}

	/** Finds the click method by name, because it differs between 26.x versions. */
	private static void click(MultiPlayerGameMode gm, int containerId, int slot, LocalPlayer p) {
		try {
			if (clickMethod == null) {
				for (String name : new String[] {"handleContainerInput", "handleInventoryMouseClick"}) {
					for (Method m : MultiPlayerGameMode.class.getDeclaredMethods()) {
						if (m.getName().equals(name) && m.getParameterCount() == 5) {
							m.setAccessible(true);
							clickMethod = m;
							break;
						}
					}
					if (clickMethod != null) break;
				}
			}
			if (clickMethod == null) {
				clickFailed = true;
				return;
			}
			clickMethod.invoke(gm, containerId, slot, 0, ContainerInput.PICKUP, p);
		} catch (Exception e) {
			clickFailed = true;
		}
	}
}
