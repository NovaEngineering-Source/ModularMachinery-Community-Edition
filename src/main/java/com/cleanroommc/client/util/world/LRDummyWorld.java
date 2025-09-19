package com.cleanroommc.client.util.world;

import com.cleanroommc.client.util.TrackedDummyWorld;

public class LRDummyWorld {

    private TrackedDummyWorld left;
    private TrackedDummyWorld right;

    private boolean useLeft = true;

    public LRDummyWorld(TrackedDummyWorld left, TrackedDummyWorld right) {
        this.left = left;
        this.right = right;
    }

    public TrackedDummyWorld getWorld() {
        return useLeft ? left : right;
    }

    public LRDummyWorld setWorld(final TrackedDummyWorld world) {
        return useLeft ? setLeft(world) : setRight(world);
    }

    public TrackedDummyWorld getAnotherWorld() {
        return useLeft ? right : left;
    }

    public LRDummyWorld setAnotherWorld(final TrackedDummyWorld world) {
        return useLeft ? setRight(world) : setLeft(world);
    }

    public boolean isUseLeft() {
        return useLeft;
    }

    public LRDummyWorld setUseLeft(final boolean useLeft) {
        this.useLeft = useLeft;
        return this;
    }

    public boolean isUseRight() {
        return !useLeft;
    }

    public LRDummyWorld setUseRight(final boolean useRight) {
        this.useLeft = !useRight;
        return this;
    }

    public TrackedDummyWorld getLeft() {
        return left;
    }

    public LRDummyWorld setLeft(final TrackedDummyWorld left) {
        this.left = left;
        return this;
    }

    public TrackedDummyWorld getRight() {
        return right;
    }

    public LRDummyWorld setRight(final TrackedDummyWorld right) {
        this.right = right;
        return this;
    }
}
