package entities.base;

public abstract class ResourceEntity extends Entity {
    protected int currentAmount;
    protected int maxAmount;
    protected int regenPerTick;
    protected int restoreAmount;
    protected boolean infinite;
    protected int hungerRecoveryAmount = 10;
    protected int thirstRecoveryAmount = 10;

    protected ResourceEntity(int x, int y, int maxAmount, int regenPerTick) {
        super(x, y);
        this.maxAmount = Math.max(1, maxAmount);
        this.currentAmount = this.maxAmount;
        this.regenPerTick = Math.max(0, regenPerTick);
        this.restoreAmount = Math.max(1, regenPerTick == 0 ? 10 : regenPerTick);
        this.hungerRecoveryAmount = 10;
        this.thirstRecoveryAmount = 10;
        this.infinite = false;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getRestoreAmount() {
        return restoreAmount;
    }

    public void setRestoreAmount(int restoreAmount) {
        this.restoreAmount = Math.max(1, restoreAmount);
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
        if (infinite) {
            this.currentAmount = this.maxAmount;
        }
    }

    public int getHungerRecoveryAmount() {
        return hungerRecoveryAmount;
    }

    public void setHungerRecoveryAmount(int hungerRecoveryAmount) {
        this.hungerRecoveryAmount = Math.max(0, hungerRecoveryAmount);
    }

    public int getThirstRecoveryAmount() {
        return thirstRecoveryAmount;
    }

    public void setThirstRecoveryAmount(int thirstRecoveryAmount) {
        this.thirstRecoveryAmount = Math.max(0, thirstRecoveryAmount);
    }

    public int consumeResource(int amount) {
        amount = Math.max(0, amount);
        if (infinite) {
            currentAmount = maxAmount;
            return amount;
        }

        int consumed = Math.min(amount, currentAmount);
        currentAmount = Math.max(0, currentAmount - amount);
        return consumed;
    }

    public void updateResourceState() {
        if (infinite) {
            currentAmount = maxAmount;
            return;
        }

        if (regenPerTick <= 0) return;
        if (currentAmount < maxAmount) {
            currentAmount = Math.min(maxAmount, currentAmount + regenPerTick);
        }
    }

    public boolean isDepleted() {
        return !infinite && currentAmount <= 0;
    }
}
