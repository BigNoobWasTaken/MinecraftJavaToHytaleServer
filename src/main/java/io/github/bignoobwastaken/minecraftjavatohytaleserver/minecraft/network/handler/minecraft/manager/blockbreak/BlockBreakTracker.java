package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.blockbreak;

import org.cloudburstmc.math.vector.Vector3i;

import java.util.concurrent.ScheduledFuture;

public class BlockBreakTracker {
    public Vector3i position;
    public long startTime;
    public ScheduledFuture<?> damageTask;
    public int sequence;

    public BlockBreakTracker(Vector3i position, int sequence) {
        this.position = position;
        this.sequence = sequence;
        this.startTime = System.currentTimeMillis();
    }

    public void cancel() {
        if (damageTask != null && !damageTask.isDone()) {
            damageTask.cancel(false);
        }
    }
}
