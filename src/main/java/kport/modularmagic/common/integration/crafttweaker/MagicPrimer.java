package kport.modularmagic.common.integration.crafttweaker;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipePrimer;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.modularmagic.common.crafting.requirement.*;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;
import thaumcraft.api.aspects.Aspect;

@ZenRegister
@ZenExpansion("mods.modularmachinery.RecipePrimer")
public class MagicPrimer {

    @ZenMethod
    public static RecipePrimer addAspectInput(RecipePrimer primer, String aspectString, int amount) {
        Aspect aspect = Aspect.getAspect(aspectString);
        if (aspect != null)
            primer.appendComponent(new RequirementAspect(IOType.INPUT, amount, aspect));
        else
            CraftTweakerAPI.logError("Invalid aspect name : " + aspectString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAspectOutput(RecipePrimer primer, String aspectString, int amount) {
        Aspect aspect = Aspect.getAspect(aspectString);
        if (aspect != null)
            primer.appendComponent(new RequirementAspect(IOType.OUTPUT, amount, aspect));
        else
            CraftTweakerAPI.logError("Invalid aspect name : " + aspectString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraInput(RecipePrimer primer, String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null)
            primer.appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        else
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraOutput(RecipePrimer primer, String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null)
            primer.appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        else
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraInput(RecipePrimer primer, String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null)
            primer.appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), max, min));
        else
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraOutput(RecipePrimer primer, String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null)
            primer.appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), max, min));
        else
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addGridPowerInput(RecipePrimer primer, int amount) {
        if (amount > 0)
            primer.appendComponent(new RequirementGrid(IOType.INPUT, amount));
        else
            CraftTweakerAPI.logError("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addGridPowerOutput(RecipePrimer primer, int amount) {
        if (amount > 0)
            primer.appendComponent(new RequirementGrid(IOType.OUTPUT, amount));
        else
            CraftTweakerAPI.logError("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addRainbowInput(RecipePrimer primer) {
        primer.appendComponent(new RequirementRainbow());
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addLifeEssenceInput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0)
            primer.appendComponent(new RequirementLifeEssence(IOType.INPUT, amount, perTick));
        else
            CraftTweakerAPI.logError("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addLifeEssenceOutput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0)
            primer.appendComponent(new RequirementLifeEssence(IOType.OUTPUT, amount, perTick));
        else
            CraftTweakerAPI.logError("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillInput(RecipePrimer primer, String willTypeString, int amount) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null)
            primer.appendComponent(new RequirementWill(IOType.INPUT, amount, willType, Integer.MIN_VALUE, Integer.MAX_VALUE));
        else
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillOutput(RecipePrimer primer, String willTypeString, int amount) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null)
            primer.appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, Integer.MIN_VALUE, Integer.MAX_VALUE));
        else
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillInput(RecipePrimer primer, String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null)
            primer.appendComponent(new RequirementWill(IOType.INPUT, amount, willType, min, max));
        else
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillOutput(RecipePrimer primer, String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null)
            primer.appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, min, max));
        else
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addImpetusInput(RecipePrimer primer, int amount) {
        primer.appendComponent(new RequirementImpetus(IOType.INPUT, amount));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addImpetusOutput(RecipePrimer primer, int amount) {
        primer.appendComponent(new RequirementImpetus(IOType.OUTPUT, amount));
        return primer;
    }
}
