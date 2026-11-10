package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.entity;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ByteEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata;

public class TextEntity extends Entity {
    public TextEntity(int id) {
        super(id);
        this.setType(131);
    }

    public void setText(String text) {
        ObjectEntityMetadata<Component> componentObjectEntityMetadata = new ObjectEntityMetadata<>(23, MetadataTypes.COMPONENT, Component.text(text));
        this.getMetadataList().setMetadata(componentObjectEntityMetadata);
    }

    public void setFace(byte face) {
        ByteEntityMetadata byteEntityMetadata = new ByteEntityMetadata(15, MetadataTypes.BYTE, face);
        this.getMetadataList().setMetadata(byteEntityMetadata);
    }

    public void setLineWidth(int width) {
        IntEntityMetadata intEntityMetadata = new IntEntityMetadata(24, MetadataTypes.INT, width);
        this.getMetadataList().setMetadata(intEntityMetadata);
    }

    public void setBackgroundColor(int a, int r, int g, int b) {
        int color = (a << 24) | (r << 16) | (g << 8) | b;
        IntEntityMetadata intEntityMetadata = new IntEntityMetadata(23, MetadataTypes.INT, color);
        this.getMetadataList().setMetadata(intEntityMetadata);
    }
}
