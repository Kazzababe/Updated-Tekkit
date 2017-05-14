package ravioli.gravioli.tekkit.machines.transport;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import ravioli.gravioli.tekkit.Tekkit;
import ravioli.gravioli.tekkit.database.DatabaseObject;
import ravioli.gravioli.tekkit.machines.SimpleMachine;

public abstract class Pipe extends SimpleMachine implements TransportReceiver {
    public TransportController transportController;
    @DatabaseObject
    public TransportItemSet itemSet = new TransportItemSet();

    @Override
    public void update() {
        ((VisibleTransportController) transportController).update();
    }

    @Override
    public void onEnable() {
        transportController = !Tekkit.VISIBLE_TRANSPORT? new HiddenTransportController(this) : new VisibleTransportController(this);
        if (transportController instanceof VisibleTransportController) {
            startTask(getTickRate());
        }
        getWorld().getNearbyEntities(getLocation(), 2.0, 2.0, 2.0).stream().filter(entity -> entity instanceof ArmorStand && !entity.hasMetadata("display") && ((ArmorStand) entity).isMarker()).forEach(entity -> entity.remove());
    }

    /**
     * Add's the transport item into the pipes transport controller.
     * NOTE: Only applies to VisibleTransportControllers
     *
     * @param item the transport item to inject
     * @param input the direction the item is being input from
     */
    public void addTransportItem(TransportItem item, BlockFace input) {
        if (transportController instanceof VisibleTransportController) {
            ((VisibleTransportController) transportController).addItem(item, input);
        }
    }

    public void addItem(ItemStack item, BlockFace input) {
        transportController.addItem(item, input);
    }

    @Override
    public boolean acceptableInput(BlockFace input) {
        return true;
    }

    @Override
    public boolean acceptableOutput(BlockFace output) {
        return true;
    }

    @Override
    public boolean canReceiveItem(ItemStack item, BlockFace input) {
        return acceptableInput(input);
    }

    @Override
    public boolean canInteract() {
        return true;
    }

    @Override
    public void onDestroy() {
        if (transportController instanceof VisibleTransportController) {
            ((VisibleTransportController) transportController).destroy();
        }
    }

    abstract double getSpeed();

    abstract int getTickRate();
}
