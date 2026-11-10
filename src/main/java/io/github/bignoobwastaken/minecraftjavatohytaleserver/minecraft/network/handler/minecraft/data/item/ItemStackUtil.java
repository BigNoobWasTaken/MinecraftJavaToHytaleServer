package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.item;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.MappingRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.ItemUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.LanguageUtil;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemStackUtil {
    public static ItemStack EMPTY = new ItemStack(0, 0, new DataComponents(new HashMap<>()));

    public static ItemStack fromHytale(ItemWithAllMetadata item) {
        int itemId = MappingRegistry.INSTANCE.getItemMapper().getMapping(item.itemId);
        Item itemConfig = ItemUtil.getItemConfig(item.itemId);
        if (itemConfig == null) return ItemStackUtil.EMPTY;

        String name = LanguageUtil.getMessage(itemConfig.getTranslationProperties().getName());
        String desc = LanguageUtil.getMessage(itemConfig.getTranslationProperties().getDescription());

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§8ID: " + item.itemId));

        if (desc != null) {
            lore.add(Component.text("§r"));
            ItemUtil.hytaleItemDescToList(desc).forEach(hytaleItem -> lore.add(Component.text(hytaleItem)));
        }

        int maxStack = itemConfig.getMaxStack();
        if (maxStack > 99) maxStack = 99;

        ItemStack itemStack = new ItemStack(itemId, item.quantity, new DataComponents(new HashMap<>()));
        assert itemStack.getDataComponentsPatch() != null;
        itemStack.getDataComponentsPatch().put(DataComponentTypes.ITEM_NAME, Component.text("§f"+name));
        itemStack.getDataComponentsPatch().put(DataComponentTypes.MAX_STACK_SIZE, maxStack);
        itemStack.getDataComponentsPatch().put(DataComponentTypes.LORE, lore);

        if (item.maxDurability > 0) {
            itemStack.getDataComponentsPatch().put(DataComponentTypes.MAX_DAMAGE, (int) item.maxDurability);
            itemStack.getDataComponentsPatch().put(DataComponentTypes.DAMAGE, (int) (item.maxDurability-item.durability));
        }

        return itemStack;
    }

    public static ItemStack fromHytaleItemId(String itemId) {
        return fromHytale(new ItemWithAllMetadata(itemId, 1, 1, 1, 1, false, null));
    }
}
