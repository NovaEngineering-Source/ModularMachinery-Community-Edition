package github.kasuminova.mmce.common.container.handler;

import github.kasuminova.mmce.common.container.ContainerMEItemInputBus;
import github.kasuminova.mmce.common.network.PktMEInputBusRecipeTransfer;
import hellfirepvp.modularmachinery.ModularMachinery;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

public class MEInputRecipeTransferHandler implements IRecipeTransferHandler<ContainerMEItemInputBus> {

    @Override
    public Class<ContainerMEItemInputBus> getContainerClass() {
        return ContainerMEItemInputBus.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerMEItemInputBus containerMEItemInputBus, IRecipeLayout recipeLayout, EntityPlayer entityPlayer, boolean maxTransfer, boolean doTransfer) {
        final String recipeType = recipeLayout.getRecipeCategory().getUid();

        // MM recipes are identified by this prefix
        if (!recipeType.contains("modularmachinery.recipe")) {
            return RecipeTransferErrorInternal.INSTANCE;
        }

        if (!doTransfer) {
            return null;
        }

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

        ArrayList<ItemStack> inputs = new ArrayList<>();
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            // We don't care about outputs
            if (ingredient.isInput()) {
                inputs.add(ingredient.getDisplayedIngredient());
            }
        }

        if (inputs.isEmpty()) {
            return RecipeTransferErrorInternal.INSTANCE;
        }

        ArrayList<ItemStack> nonNullInputs = new ArrayList<>();
        for (ItemStack input : inputs) {
            if (input != null) {
                nonNullInputs.add(input);
            }
        }

        PktMEInputBusRecipeTransfer pktMEInputBusRecipeTransfer = new PktMEInputBusRecipeTransfer(nonNullInputs);
        ModularMachinery.NET_CHANNEL.sendToServer(pktMEInputBusRecipeTransfer);

        return null;
    }
}
