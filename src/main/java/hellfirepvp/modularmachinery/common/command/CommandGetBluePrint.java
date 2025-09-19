package hellfirepvp.modularmachinery.common.command;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandGetBluePrint extends CommandBase {
    @Override
    public String getName() {
        return "mm-get_blueprint";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.modularmachinery.get_blueprint";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!(sender instanceof final EntityPlayer player)) {
            sender.sendMessage(new TextComponentTranslation("command.modularmachinery.get_blueprint.player_only"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(new TextComponentTranslation(getUsage(sender)));
            return;
        }

        String machineName = args[0];

        DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
        if (machine == null) {
            sender.sendMessage(new TextComponentTranslation(
                "command.modularmachinery.get_blueprint.not_found", machineName));
            return;
        }

        ItemStack blueprint = new ItemStack(ItemsMM.blueprint);
        ItemBlueprint.setAssociatedMachine(blueprint, machine);

        if (player.addItemStackToInventory(blueprint)) {
            sender.sendMessage(
                new TextComponentTranslation("command.modularmachinery.get_blueprint.success"));
        }
    }
}
