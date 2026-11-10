package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.entity;

//import dev.hytalemodding.minecraft.network.handler.minecraft.data.entity.metadata.*;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.*;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.FloatEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.*;

import java.util.*;

@Getter
@Setter
public class Entity {
    private int id;
    private UUID uuid;
    private int type;

    private double x;
    private double y;
    private double z;

    private byte yaw;
    private byte pitch;
    private byte headYaw;

    //private EntityMetadata metadata;
    protected class MetadataList {
        private Map<Integer, EntityMetadata<?, ?>> metadataMap = new HashMap<>();
        private Set<Integer> dirtyIds = new HashSet<>();

        public void setMetadata(EntityMetadata<?, ?> metadata) {
            this.metadataMap.put(metadata.getId(), metadata);
            this.dirtyIds.add(metadata.getId());
        }

        public EntityMetadata<?, ?> getMetadata(int id) {
            return this.metadataMap.get(id);
        }

        public EntityMetadata<?, ?>[] pushDirtyMetadata() {
            List<EntityMetadata<?, ?>> metadatas = new ArrayList<>(dirtyIds.size());
            this.dirtyIds.forEach(dirtyId -> metadatas.add(metadataMap.get(dirtyId)));
            this.dirtyIds.clear();
            return metadatas.toArray(new EntityMetadata[0]);
        }
    }
    private MetadataList metadataList;
    private Map<EquipmentSlot, ItemStack> equipment;

    private boolean onGround;

    private int data;

    private boolean spawned;

    public Entity(int id) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.type = -1;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
        this.headYaw = 0;
        this.data = 0;
        this.metadataList = new MetadataList();
        this.equipment = new HashMap<>();
    }

    public void spawn(MinecraftClientConnection connection) {
        if (type == -1) type = 0;

        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                this.id,
                this.uuid,
                EntityType.from(type),
                this.x,
                this.y,
                this.z,
                this.yaw,
                this.pitch,
                this.headYaw
        );
        connection.getSession().send(spawnPacket);

        sendMetadata(connection);
        sendEquipment(connection);
        spawned = true;
    }

    public void sendMetadata(MinecraftClientConnection connection) {
        if (metadataList == null || metadataList.metadataMap.isEmpty()) return;
        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(this.id, this.metadataList.pushDirtyMetadata());
        connection.getSession().send(metadataPacket);
    }

    public void sendEquipment(MinecraftClientConnection connection) {
        if (equipment.isEmpty()) return;
        List<Equipment> equipmentList = new ArrayList<>();
        equipment.forEach(((equipmentSlot, itemStack) -> equipmentList.add(new Equipment(equipmentSlot, itemStack))));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(id, equipmentList.toArray(new Equipment[0]));
        connection.getSession().send(packet);
    }

    public void despawn(MinecraftClientConnection connection) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(new int[]{getId()});
        connection.getSession().send(packet);
        spawned = false;
    }

    public void setHealth(float health) {
        if (health < 0) health = 0;
        FloatEntityMetadata healthMetadata = new FloatEntityMetadata(9, MetadataTypes.FLOAT, health);
        metadataList.setMetadata(healthMetadata);
    }

    public float getHealth() {
        EntityMetadata<?, ?> metadataValue = metadataList.getMetadata(9);
        if (metadataValue == null) return 0;
        return ((FloatEntityMetadata) metadataValue).getPrimitiveValue();
    }

    public void setEquipment(EquipmentSlot type, ItemStack item) {
        equipment.put(type, item);
    }

    public void setItem(ItemStack item) {
        ObjectEntityMetadata<ItemStack> itemStackMetadata = new ObjectEntityMetadata<>(8, MetadataTypes.ITEM_STACK, item);
        metadataList.setMetadata(itemStackMetadata);
    }

    public void setPose(Pose pose) {
        ObjectEntityMetadata<Pose> poseMetadata = new ObjectEntityMetadata<>(6, MetadataTypes.POSE, pose);
        metadataList.setMetadata(poseMetadata);
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setRotation(float yaw, float pitch) {
        this.pitch = (byte) (pitch * 256.0F / 360.0F);
        this.yaw = (byte) (yaw * 256.0F / 360.0F);
    }

    public void setHeadYaw(float yaw) {
        this.headYaw = (byte) (yaw * 256.0F / 360.0F);
    }

    public void teleport(MinecraftClientConnection connection, Vector3d pos, Vector3d deltaPos, float yaw, float pitch) {
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(id, pos, deltaPos, yaw, pitch, new ArrayList<>(), onGround);
        connection.getSession().send(packet);
        setPosition(x, y, z);
        setRotation(yaw, pitch);
    }

    public void move(MinecraftClientConnection connection, double toX, double toY, double toZ) {
        ClientboundMoveEntityPosPacket packet = new ClientboundMoveEntityPosPacket(id, toX-x, toY-y, toZ-z, onGround);
        connection.getSession().send(packet);
        setPosition(toX, toY, toZ);
    }

    public void moveAndRot(MinecraftClientConnection connection, double toX, double toY, double toZ, float yaw, float pitch) {
        ClientboundMoveEntityPosRotPacket packet = new ClientboundMoveEntityPosRotPacket(id, toX-x, toY-y, toZ-z, yaw, pitch, onGround);
        connection.getSession().send(packet);
        setPosition(toX, toY, toZ);
        setRotation(yaw, pitch);
    }

    public void rotate(MinecraftClientConnection connection, float yaw, float pitch) {
        ClientboundMoveEntityRotPacket packet = new ClientboundMoveEntityRotPacket(id, yaw, pitch, onGround);
        connection.getSession().send(packet);
        setRotation(yaw, pitch);
    }

    public void rotateHead(MinecraftClientConnection connection, float headYaw) {
        ClientboundRotateHeadPacket headPacket = new ClientboundRotateHeadPacket(id, headYaw);
        connection.getSession().send(headPacket);
        setHeadYaw(headYaw);
    }

    public void setCustomName(String name) {
        ObjectEntityMetadata<Optional<Component>> componentObjectEntityMetadata = new ObjectEntityMetadata<>(2, MetadataTypes.OPTIONAL_COMPONENT, Optional.of(Component.text(name)));
        metadataList.setMetadata(componentObjectEntityMetadata);
        BooleanEntityMetadata booleanEntityMetadata = new BooleanEntityMetadata(3, MetadataTypes.BOOLEAN, true);
        metadataList.setMetadata(booleanEntityMetadata);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", headYaw=" + headYaw +
                ", data=" + data +
                '}';
    }
}
