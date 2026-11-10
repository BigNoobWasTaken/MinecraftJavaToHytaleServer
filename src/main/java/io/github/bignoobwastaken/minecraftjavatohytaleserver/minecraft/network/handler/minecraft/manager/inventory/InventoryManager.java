package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.inventory;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.protocol.packets.inventory.MoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleClientPacketHandlerRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.*;

import javax.annotation.Nullable;

public class InventoryManager {
    public static InventoryManager INSTANCE = new InventoryManager();

    private static final String EMPTY_ITEM_KEY = "Empty";

    private InventoryManager() {}

    @Nullable
    public static ItemContainer getSectionById(ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, int sectionId) {
        if (sectionId >= 0) {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            if (playerComponent != null) {
                Window window = playerComponent.getWindowManager().getWindow(sectionId);
                if (window instanceof ItemContainerWindow itemContainerWindow) {
                    return itemContainerWindow.getItemContainer();
                }
            }
            return null;
        }
        ComponentType<EntityStore, ? extends InventoryComponent> type = InventoryComponent.getComponentTypeById(sectionId);
        if (type == null) return null;
        InventoryComponent component = store.getComponent(ref, type);
        return component != null ? component.getInventory() : null;
    }

    public void handleClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short slot, ContainerAction button, ContainerActionType mode) {
        if (mode.equals(ContainerActionType.SPREAD_ITEM)) {
            if (slot == -999) return;
            mode = ContainerActionType.CLICK_ITEM;
            if (button.getId() == 1) button = ClickItemAction.LEFT_CLICK;
            else if (button.getId() == 5) button = ClickItemAction.RIGHT_CLICK;
        }

        if (slot == -999) {
            handleClickOutside(connection, store, ref);
            return;
        }

        switch (mode) {
            case CLICK_ITEM -> handleNormalClick(connection, store, ref, slot, (ClickItemAction) button);
            case SHIFT_CLICK_ITEM -> handleShiftClick(connection, store, ref, slot);
            case MOVE_TO_HOTBAR_SLOT -> handleNumberKey(connection, store, ref, slot, (MoveToHotbarAction) button);
            case DROP_ITEM -> handleDrop(connection, store, ref, slot, (DropItemAction) button);
            case FILL_STACK -> handleDoubleClick(connection, store, ref, slot);
        }
    }

    public void handleContainerClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short slot, ContainerAction button, ContainerActionType mode, int containerId, int guiSlotCount) {
        if (mode.equals(ContainerActionType.SPREAD_ITEM)) {
            if (slot == -999) return;
            mode = ContainerActionType.CLICK_ITEM;
            if (button.getId() == 1) button = ClickItemAction.LEFT_CLICK;
            else if (button.getId() == 5) button = ClickItemAction.RIGHT_CLICK;
        }

        if (slot == -999) {
            handleClickOutside(connection, store, ref);
            return;
        }

        switch (mode) {
            case CLICK_ITEM -> handleNormalClick(connection, store, ref, slot, (ClickItemAction) button, containerId, guiSlotCount);
            case SHIFT_CLICK_ITEM -> handleShiftClick(connection, store, ref, slot, containerId, guiSlotCount);
            case MOVE_TO_HOTBAR_SLOT -> handleNumberKey(connection, store, ref, slot, (MoveToHotbarAction) button, containerId, guiSlotCount);
            case DROP_ITEM -> handleDrop(connection, store, ref, slot, (DropItemAction) button, containerId, guiSlotCount);
            case FILL_STACK -> handleDoubleClick(connection, store, ref, slot, containerId, guiSlotCount);
        }
    }

    private void handleClickOutside(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref) {
        InventoryCursor cursor = connection.getCursor();
        if (cursor.heldItem == null) return;

        resyncInventory(connection, store, ref);
    }

    private void handleNormalClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, ClickItemAction button) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot);
        if (hytaleSlot == null) {
            resyncInventory(connection, store, ref);
            return;
        }

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        switch (button) {
            case LEFT_CLICK -> handleLeftClick(connection, mcSlot, hytaleSlot, slotItem);
            case RIGHT_CLICK -> handleRightClick(connection, mcSlot, hytaleSlot, slotItem);
        }
    }

    private void clearCursor(InventoryCursor cursor) {
        cursor.heldItem = null;
        cursor.lastClickedSection = -1;
        cursor.lastClickedSlot = -1;
        cursor.itemSlot = -1;
    }

    private void handleLeftClick(MinecraftClientConnection connection, short mcSlot, HytaleSlot hytaleSlot, ItemStack slotItem) {
        InventoryCursor cursor = connection.getCursor();
        boolean slotEmpty = isHytaleItemEmpty(slotItem);
        boolean cursorEmpty = cursor.heldItem == null;

        if (cursorEmpty && !slotEmpty) {
            cursor.heldItem = slotItem;
            cursor.lastClickedSection = hytaleSlot.sectionId;
            cursor.lastClickedSlot = hytaleSlot.slotId;
            cursor.itemSlot = mcSlot;
            return;
        }

        if (!cursorEmpty) {
            if (slotEmpty) {
                MoveItemStack movePacket = new MoveItemStack(
                        cursor.lastClickedSection, cursor.lastClickedSlot,
                        cursor.heldItem.getQuantity(),
                        hytaleSlot.sectionId, hytaleSlot.slotId
                );
                connection.getHytaleChannel().sendPacket(movePacket);
                clearCursor(cursor);
            } else  {
                boolean sameType = cursor.heldItem.getItemId().equals(slotItem.getItemId());

                if (sameType) {
                    int total = cursor.heldItem.getQuantity() + slotItem.getQuantity();
                    int maxStack = cursor.heldItem.getItem().getMaxStack();

                    if (total <= maxStack) {
                        MoveItemStack movePacket = new MoveItemStack(
                                cursor.lastClickedSection, cursor.lastClickedSlot,
                                cursor.heldItem.getQuantity(),
                                hytaleSlot.sectionId, hytaleSlot.slotId
                        );
                        connection.getHytaleChannel().sendPacket(movePacket);
                        clearCursor(cursor);
                    } else {
                        int canMove = maxStack - slotItem.getQuantity();
                        if (canMove > 0) {
                            MoveItemStack movePacket = new MoveItemStack(
                                    cursor.lastClickedSection, cursor.lastClickedSlot,
                                    canMove,
                                    hytaleSlot.sectionId, hytaleSlot.slotId
                            );
                            connection.getHytaleChannel().sendPacket(movePacket);
                            cursor.heldItem = cursor.heldItem.withQuantity(cursor.heldItem.getQuantity() - canMove);
                        }
                    }
                } else {
                    MoveItemStack movePacket = new MoveItemStack(
                            cursor.lastClickedSection, cursor.lastClickedSlot,
                            cursor.heldItem.getQuantity(),
                            hytaleSlot.sectionId, hytaleSlot.slotId
                    );
                    connection.getHytaleChannel().sendPacket(movePacket);

                    cursor.heldItem = slotItem;
                    if (cursor.itemSlot < 0) cursor.itemSlot = mcSlot;
                }
            }
        }
    }

    private void handleRightClick(MinecraftClientConnection connection, short mcSlot, HytaleSlot hytaleSlot, ItemStack slotItem) {
        InventoryCursor cursor = connection.getCursor();
        boolean slotEmpty = isHytaleItemEmpty(slotItem);
        boolean cursorEmpty = cursor.heldItem == null;

        if (cursorEmpty && !slotEmpty) {
            int totalQuantity = slotItem.getQuantity();
            int pickUpAmount = (totalQuantity + 1) / 2;

            cursor.heldItem = slotItem.withQuantity(pickUpAmount);
            cursor.lastClickedSection = hytaleSlot.sectionId;
            cursor.lastClickedSlot = hytaleSlot.slotId;
            cursor.itemSlot = mcSlot;
            return;
        }

        if (!cursorEmpty) {
            if (slotEmpty) {
                MoveItemStack movePacket = new MoveItemStack(
                        cursor.lastClickedSection, cursor.lastClickedSlot,
                        1,
                        hytaleSlot.sectionId, hytaleSlot.slotId
                );
                connection.getHytaleChannel().sendPacket(movePacket);

                if (cursor.heldItem.getQuantity() <= 1) {
                    clearCursor(cursor);
                } else {
                    cursor.heldItem = cursor.heldItem.withQuantity(cursor.heldItem.getQuantity() - 1);
                }
            } else  {
                boolean sameType = cursor.heldItem.getItemId().equals(slotItem.getItemId());

                if (sameType) {
                    int maxStack = slotItem.getItem().getMaxStack();
                    if (slotItem.getQuantity() < maxStack) {
                        MoveItemStack movePacket = new MoveItemStack(
                                cursor.lastClickedSection, cursor.lastClickedSlot,
                                1,
                                hytaleSlot.sectionId, hytaleSlot.slotId
                        );
                        connection.getHytaleChannel().sendPacket(movePacket);

                        if (cursor.heldItem.getQuantity() <= 1) {
                            clearCursor(cursor);
                        } else {
                            cursor.heldItem = cursor.heldItem.withQuantity(cursor.heldItem.getQuantity() - 1);
                        }
                    }
                } else {
                    MoveItemStack movePacket = new MoveItemStack(
                            cursor.lastClickedSection, cursor.lastClickedSlot,
                            cursor.heldItem.getQuantity(),
                            hytaleSlot.sectionId, hytaleSlot.slotId
                    );
                    connection.getHytaleChannel().sendPacket(movePacket);

                    cursor.heldItem = slotItem;
                    if (cursor.itemSlot < 0) cursor.itemSlot = mcSlot;
                }
            }
        }
    }

    private void handleNumberKey(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, MoveToHotbarAction button) {
        InventoryCursor cursor = connection.getCursor();
        if (button.getId() < 0 || button.getId() > 8) return;

        HytaleSlot clickedSlot = mcSlotToHytale(mcSlot);
        if (clickedSlot == null) return;

        HytaleSlot hotbarSlot = new HytaleSlot(-1, button.getId());

        ItemContainer clickedContainer = getSectionById(store, ref, clickedSlot.sectionId);
        ItemContainer hotbarContainer = getSectionById(store, ref, hotbarSlot.sectionId);

        if (clickedContainer == null || hotbarContainer == null) return;

        ItemStack clickedItem = clickedContainer.getItemStack((short) clickedSlot.slotId);
        ItemStack hotbarItem = hotbarContainer.getItemStack((short) hotbarSlot.slotId);

        boolean clickedEmpty = isHytaleItemEmpty(clickedItem);
        boolean hotbarEmpty = isHytaleItemEmpty(hotbarItem);

        if (!clickedEmpty) {
            MoveItemStack move1 = new MoveItemStack(
                    clickedSlot.sectionId, clickedSlot.slotId,
                    clickedItem.getQuantity(),
                    hotbarSlot.sectionId, hotbarSlot.slotId
            );
            connection.getHytaleChannel().sendPacket(move1);
        } else if (!hotbarEmpty) {
            MoveItemStack move = new MoveItemStack(
                    hotbarSlot.sectionId, hotbarSlot.slotId,
                    hotbarItem.getQuantity(),
                    clickedSlot.sectionId, clickedSlot.slotId
            );
            connection.getHytaleChannel().sendPacket(move);
        }

        cursor.heldItem = null;
    }

    private void handleDoubleClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot) {
        InventoryCursor cursor = connection.getCursor();
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot);
        if (hytaleSlot == null) return;

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        if (isHytaleItemEmpty(slotItem)) return;

        SmartMoveItemStack smartMove = new SmartMoveItemStack(
                hytaleSlot.sectionId,
                hytaleSlot.slotId,
                slotItem.getQuantity(),
                SmartMoveType.EquipOrMergeStack
        );
        connection.getHytaleChannel().sendPacket(smartMove);

        cursor.heldItem = null;
    }

    private void handleShiftClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot);
        if (hytaleSlot == null) return;

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        if (slotItem == null || slotItem.isEmpty()) return;

        SmartMoveItemStack smartMove = new SmartMoveItemStack(
                hytaleSlot.sectionId,
                hytaleSlot.slotId,
                slotItem.getQuantity(),
                SmartMoveType.PutInHotbarOrWindow
        );
        connection.getHytaleChannel().sendPacket(smartMove);
    }

    private void handleDrop(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, DropItemAction button) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot);
        if (hytaleSlot == null) {
            resyncInventory(connection, store, ref);
            return;
        }

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack item = container.getItemStack((short) hytaleSlot.slotId);
        if (item == null || item.isEmpty()) {
            resyncInventory(connection, store, ref);
            return;
        }

        int quantity = button == DropItemAction.DROP_FROM_SELECTED ? 1 : item.getQuantity();

        DropItemStack packet = new DropItemStack(hytaleSlot.sectionId, hytaleSlot.slotId, quantity);
        connection.getHytaleChannel().sendPacket(packet);
    }

    private void handleNormalClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, ClickItemAction button, int containerId, int guiSlotCount) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot, containerId, guiSlotCount);
        if (hytaleSlot == null) {
            resyncInventory(connection, store, ref);
            return;
        }

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        switch (button) {
            case LEFT_CLICK -> handleLeftClick(connection, mcSlot, hytaleSlot, slotItem);
            case RIGHT_CLICK -> handleRightClick(connection, mcSlot, hytaleSlot, slotItem);
        }
    }

    private void handleShiftClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, int containerId, int guiSlotCount) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot, containerId, guiSlotCount);
        if (hytaleSlot == null) return;

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        if (slotItem == null || slotItem.isEmpty()) return;

        SmartMoveItemStack smartMove = new SmartMoveItemStack(
                hytaleSlot.sectionId,
                hytaleSlot.slotId,
                slotItem.getQuantity(),
                SmartMoveType.PutInHotbarOrWindow
        );
        connection.getHytaleChannel().sendPacket(smartMove);
    }

    private void handleNumberKey(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, MoveToHotbarAction button, int containerId, int guiSlotCount) {
        InventoryCursor cursor = connection.getCursor();
        if (button.getId() < 0 || button.getId() > 8) return;

        HytaleSlot clickedSlot = mcSlotToHytale(mcSlot, containerId, guiSlotCount);
        if (clickedSlot == null) return;

        HytaleSlot hotbarSlot = new HytaleSlot(-1, button.getId());

        ItemContainer clickedContainer = getSectionById(store, ref, clickedSlot.sectionId);
        ItemContainer hotbarContainer = getSectionById(store, ref, hotbarSlot.sectionId);

        if (clickedContainer == null || hotbarContainer == null) return;

        ItemStack clickedItem = clickedContainer.getItemStack((short) clickedSlot.slotId);
        ItemStack hotbarItem = hotbarContainer.getItemStack((short) hotbarSlot.slotId);

        boolean clickedEmpty = isHytaleItemEmpty(clickedItem);
        boolean hotbarEmpty = isHytaleItemEmpty(hotbarItem);

        if (!clickedEmpty) {
            MoveItemStack move1 = new MoveItemStack(
                    clickedSlot.sectionId, clickedSlot.slotId,
                    clickedItem.getQuantity(),
                    hotbarSlot.sectionId, hotbarSlot.slotId
            );
            connection.getHytaleChannel().sendPacket(move1);
        } else if (!hotbarEmpty) {
            MoveItemStack move = new MoveItemStack(
                    hotbarSlot.sectionId, hotbarSlot.slotId,
                    hotbarItem.getQuantity(),
                    clickedSlot.sectionId, clickedSlot.slotId
            );
            connection.getHytaleChannel().sendPacket(move);
        }

        cursor.heldItem = null;
    }

    private void handleDoubleClick(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, int containerId, int guiSlotCount) {
        InventoryCursor cursor = connection.getCursor();
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot, containerId, guiSlotCount);
        if (hytaleSlot == null) return;

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack slotItem = container.getItemStack((short) hytaleSlot.slotId);

        if (isHytaleItemEmpty(slotItem)) return;

        SmartMoveItemStack smartMove = new SmartMoveItemStack(
                hytaleSlot.sectionId,
                hytaleSlot.slotId,
                slotItem.getQuantity(),
                SmartMoveType.EquipOrMergeStack
        );
        connection.getHytaleChannel().sendPacket(smartMove);

        cursor.heldItem = null;
    }

    private void handleDrop(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, short mcSlot, DropItemAction button, int containerId, int guiSlotCount) {
        HytaleSlot hytaleSlot = mcSlotToHytale(mcSlot, containerId, guiSlotCount);
        if (hytaleSlot == null) {
            resyncInventory(connection, store, ref);
            return;
        }

        ItemContainer container = getSectionById(store, ref, hytaleSlot.sectionId);
        if (container == null) return;

        ItemStack item = container.getItemStack((short) hytaleSlot.slotId);
        if (item == null || item.isEmpty()) {
            resyncInventory(connection, store, ref);
            return;
        }

        int quantity = button == DropItemAction.DROP_FROM_SELECTED ? 1 : item.getQuantity();

        DropItemStack packet = new DropItemStack(hytaleSlot.sectionId, hytaleSlot.slotId, quantity);
        connection.getHytaleChannel().sendPacket(packet);
    }

    public void resyncInventory(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref) {
        InventoryComponent.Storage storage = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        InventoryComponent.Armor armor = store.getComponent(ref, InventoryComponent.Armor.getComponentType());
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Utility utility = store.getComponent(ref, InventoryComponent.Utility.getComponentType());
        InventoryComponent.Tool tools = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
        InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
        InventoryComponent.AbilitySlots abilitySlots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        InventoryComponent.RuneBag runeBag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());

        assert storage != null;
        assert armor != null;
        assert hotbar != null;
        assert utility != null;
        assert tools != null;
        assert backpack != null;
        assert abilitySlots != null;
        assert runeBag != null;
        UpdatePlayerInventory packet = new UpdatePlayerInventory(
                storage.getInventory().toPacket(),
                armor.getInventory().toPacket(),
                hotbar.getInventory().toPacket(),
                utility.getInventory().toPacket(),
                tools.getInventory().toPacket(),
                backpack.getInventory().toPacket(),
                abilitySlots.getInventory().toPacket(),
                runeBag.getInventory().toPacket()
        );

        HytaleClientPacketHandlerRegistry.INSTANCE.handlePacket(packet, connection);
    }

    private HytaleSlot mcSlotToHytale(short mcSlot) {
        if (mcSlot >= 0 && mcSlot <= 4) {
            return null;
        } else if (mcSlot >= 5 && mcSlot <= 8) {
            return new HytaleSlot(-3, mcSlot - 5);
        } else if (mcSlot >= 9 && mcSlot <= 35) {
            return new HytaleSlot(-2, mcSlot - 9);
        } else if (mcSlot >= 36 && mcSlot <= 44) {
            return new HytaleSlot(-1, mcSlot - 36);
        } else if (mcSlot == 45) {
            return new HytaleSlot(-5, 0);
        }
        return null;
    }

    private HytaleSlot mcSlotToHytale(short mcSlot, int containerId, int guiSlotCount) {
        if (mcSlot >= 0 && mcSlot < guiSlotCount) {
            return new HytaleSlot(containerId, mcSlot);
        }
        int offset = mcSlot - guiSlotCount;
        if (offset >= 0 && offset <= 26) {
            return new HytaleSlot(-2, offset);
        }
        if (offset >= 27 && offset <= 35) {
            return new HytaleSlot(-1, offset - 27);
        }
        return null;
    }

    private boolean isHytaleItemEmpty(ItemStack item) {
        return item == null || item.getItemId().equals(EMPTY_ITEM_KEY);
    }
}