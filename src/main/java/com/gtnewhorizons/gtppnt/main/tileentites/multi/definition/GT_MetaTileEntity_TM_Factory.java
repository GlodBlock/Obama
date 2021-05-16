package com.gtnewhorizons.gtppnt.main.tileentites.multi.definition;

import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.technus.tectech.mechanics.structure.IStructureDefinition;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyMulti;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.util.CommonValues;
import com.github.technus.tectech.util.Vec3Impl;
import com.gtnewhorizons.gtppnt.main.loaders.CasingTextureLoader;
import com.gtnewhorizons.gtppnt.main.tileentites.multi.definition.sound.ISoundProviderImpl;
import com.gtnewhorizons.gtppnt.main.tileentites.multi.definition.structure.IConstructableStructure;
import com.gtnewhorizons.gtppnt.main.tileentites.multi.definition.texture.ITextureProviderImpl;
import com.gtnewhorizons.gtppnt.main.tileentites.single.hatches.GT_MetaTileEntity_TM_HatchCasing;
import com.gtnewhorizons.gtppnt.main.tileentites.single.hatches.defenition.IFunctionalCasingMachineList;
import com.gtnewhorizons.gtppnt.main.utils.MultiBlockUtils;
import com.gtnewhorizons.gtppnt.main.utils.RecipeIterable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_Recipe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

import static com.github.technus.tectech.util.CommonValues.VN;

//TODO Slot recipe handling into its own interface
public abstract class GT_MetaTileEntity_TM_Factory extends GT_MetaTileEntity_MultiblockBase_EM implements
        IConstructableStructure,IFunctionalCasingMachineList, ITextureProviderImpl, ISoundProviderImpl {
    private final Set<GT_MetaTileEntity_TM_HatchCasing> functionalCasings = new HashSet<>();
    private byte casingTier = 0;
    private Vec3Impl structureOffset;
    private int sliceCount = 0;
    private GT_Recipe buffered_Recipe;

    //region Constructors
    public GT_MetaTileEntity_TM_Factory(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        setRepairFlags();
        registerStructure_TM();
        registerActivitySound_TM();
    }

    public GT_MetaTileEntity_TM_Factory(String aName) {
        super(aName);
        // FIXME: 25/02/2021 remove this later
        setRepairFlags();
    }

    public ItemStack getItem() {
        return this.getStackForm(1L);
    }

    protected void setRepairFlags() {
        this.mWrench = true;
        this.mScrewdriver = true;
        this.mSoftHammer = true;
        this.mHardHammer = true;
        this.mSolderingTool = true;
        this.mCrowbar = true;
    }

    @Override
    public abstract String[] getDescription();
    //endregion

    //region Structure
    @Override
    public int getTextureIndex() {
        return CasingTextureLoader.getBasicCasingTextureIndex(getCasingMeta());
    }

    @Override
    public IStructureDefinition<? extends GT_MetaTileEntity_MultiblockBase_EM> getStructure_EM() {
        return getStructure_TM();
    }

    @Override
    public Block getCasingBlock() {
        return WerkstoffLoader.BWBlockCasings;
    }

    @Override
    protected boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        functionalCasingsPreCheckMachine();
        return checkMachine_TM(iGregTechTileEntity, itemStack) && functionalCasingsPostCheckMachine();
    }

    @Override
    public boolean structureCheck_TM(String piece, int horizontalOffset, int verticalOffset, int depthOffset) {
        return structureCheck_EM(piece, horizontalOffset, verticalOffset, depthOffset);
    }

    @Override
    public boolean structureBuild_TM(String piece, int horizontalOffset, int verticalOffset, int depthOffset, boolean hintsOnly, ItemStack trigger) {
        return structureBuild_EM(piece, horizontalOffset, verticalOffset, depthOffset, hintsOnly, trigger);
    }
    //endregion

    //region Sliceable Shape Interface
    public void setCurrentStructureOffset(Vec3Impl structureOffset) {
        this.structureOffset = structureOffset;
    }

    public Vec3Impl getCurrentStructureOffset() {
        return this.structureOffset;
    }

    public int getMinParrallel() {
        return 0;
    }

    public int getMinSlices() {
        return 1;
    }

    public int getSliceCount() {
        return sliceCount;
    }

    public void setSliceCount(int sliceCount) {
        this.sliceCount = sliceCount;
    }
    //endregion

    //region Textures
    @Override
    public void registerIcons(IIconRegister aBlockIconRegister) {
        super.registerIcons(aBlockIconRegister);
        registerIcons_TM();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex,
                                 boolean aActive, boolean aRedstone) {
        return getTexture_TM(aSide, aFacing, aActive);
    }
    //endregion

    //region Sounds
    @Override
    @SideOnly(Side.CLIENT)
    protected ResourceLocation getActivitySound() {
        return getActivitySound_TM();
    }
    //endregion

    //region On Tick
    @Override
    public abstract GT_Recipe.GT_Recipe_Map getRecipeMap();

    protected boolean isPerfectOC() {
        return false;
    }

    private long getMaxVoltage() {
        long voltage = 0;
        if (getCasingTier() >= 0 && getCasingTier() <= 15) {
            voltage = CommonValues.V[getCasingTier()];
        }
        return voltage;
    }
    //TODO Allow for multiple different recipes to run at the same time
    //TODO Allow multies to have more spesilised recipes
    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        boolean canRunRecipe = false;
        if (this.getEUVar() > this.getMaxInputVoltage()) {
            ItemStack[] inputItems = this.getStoredInputs().toArray(new ItemStack[0]);
            FluidStack[] inputFluids = this.getStoredFluids().toArray(new FluidStack[0]);
            if (inputItems.length > 0 || inputFluids.length > 0) {
                ItemStack[] combinedItems = MultiBlockUtils.combineStacks(inputItems);
                RecipeIterable recipes = new RecipeIterable(
                        getRecipeMap(),
                        this.buffered_Recipe,
                        false,
                        true,
                        this.getMaxInputVoltage(),
                        inputFluids,
                        inputItems);
                //TODO Mention that getMaxParalells() is extended by IStructureProvider too
                int parrallel = getMaxParalells();
                int parrallelDone = 0;
                ArrayList<ItemStack> outputItems = new ArrayList<>();
                ArrayList<FluidStack> outputFluids = new ArrayList<>();
                for (GT_Recipe recipe: recipes) {
                    if (recipe != null) {
                        if (recipe.mCanBeBuffered) {
                            this.buffered_Recipe = recipe;
                        }
                        parrallelDone = MultiBlockUtils.isRecipeEqualAndRemoveParrallel(recipe,
                                inputItems,combinedItems,inputFluids,parrallel,true);

                        //TODO Add chance calculation to outputs
                        MultiBlockUtils.addItemOutputToList(recipe,outputItems,parrallelDone);
                        MultiBlockUtils.addFluidoutputToList(recipe,outputFluids,parrallelDone);
                        if (parrallelDone > 0) {
                            parrallel -= parrallelDone;
                            if (setEnergy(recipe,parrallelDone))
                                canRunRecipe = true;
                        }
                    }
                    if (parrallelDone >0)
                        break;
                }
                if (canRunRecipe) {
                    this.mOutputItems = MultiBlockUtils.sortOutputItemStacks(outputItems);
                    this.mOutputFluids = MultiBlockUtils.sortOutputFluidStacks(outputFluids);
                    this.updateSlots();
                }
            }
        }

        return canRunRecipe;
    }

    private boolean setEnergy(GT_Recipe recipe,int parrallel) {
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        this.calculateOverclockedNessMultiInternal(recipe.mEUt * parrallel, recipe.mDuration / 50, getRecipeMap().mAmperage * parrallel, getMaxVoltage(), isPerfectOC());
        // FIXME: 26/02/2021 Undo duration debug boost
        if (mMaxProgresstime != Integer.MAX_VALUE - 1 && mEUt != Integer.MAX_VALUE - 1) {
            if (this.mEUt > 0) {
                this.mEUt *= -1;
            }
            this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
            return true;
        }
        return false;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        //TODO Dont Call This Evry Tick
        onPostTickFunctionalCasing(aBaseMetaTileEntity);
    }

    @Override
    public void stopMachine() {
        super.stopMachine();
    }
    //endregion

//    @Override
//    public int getSliceCount() {
//        return sliceCount;
//    }

//    @Override
//    public void setSliceCount(int sliceCount) {
//        this.sliceCount = sliceCount;
//    }

//    @Override
//    public int getMinSlices() {
//        return 1;
//    }

    @Override
    public byte getCasingTier() {
        return casingTier;
    }

    @Override
    public void setCasingTier(byte casingTier) {
        this.casingTier = casingTier;
    }

    @Override
    public Set<GT_MetaTileEntity_TM_HatchCasing> getFunctionalCasings() {
        return functionalCasings;
    }

    @Override
    public String[] getInfoData() {//TODO Do it
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (GT_MetaTileEntity_Hatch_Energy tHatch : mEnergyHatches) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }
        for (GT_MetaTileEntity_Hatch_EnergyMulti tHatch : eEnergyMulti) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }

        return new String[]{
                "Progress:",
                EnumChatFormatting.GREEN + Integer.toString(mProgresstime / 20) + EnumChatFormatting.RESET + " s / " +
                        EnumChatFormatting.YELLOW + mMaxProgresstime / 20 + EnumChatFormatting.RESET + " s",
                "Energy Hatches:",
                EnumChatFormatting.GREEN + Long.toString(storedEnergy) + EnumChatFormatting.RESET + " EU / " +
                        EnumChatFormatting.YELLOW + maxEnergy + EnumChatFormatting.RESET + " EU",
                (mEUt * eAmpereFlow <= 0 ? "Probably uses: " : "Probably makes: ") +
                        EnumChatFormatting.RED + Math.abs(mEUt) + EnumChatFormatting.RESET + " EU/t at " +
                        EnumChatFormatting.RED + eAmpereFlow + EnumChatFormatting.RESET + " A",
                "Tier Rating: " + EnumChatFormatting.YELLOW + VN[getMaxEnergyInputTier_EM()] + EnumChatFormatting.RESET + " / " + EnumChatFormatting.GREEN + VN[getMinEnergyInputTier_EM()] + EnumChatFormatting.RESET +
                        " Amp Rating: " + EnumChatFormatting.GREEN + eMaxAmpereFlow + EnumChatFormatting.RESET + " A",
                "Problems: " + EnumChatFormatting.RED + (getIdealStatus() - getRepairStatus()) + EnumChatFormatting.RESET +
                        " Efficiency: " + EnumChatFormatting.YELLOW + mEfficiency / 100.0F + EnumChatFormatting.RESET + " %",
                "PowerPass: " + EnumChatFormatting.BLUE + ePowerPass + EnumChatFormatting.RESET +
                        " SafeVoid: " + EnumChatFormatting.BLUE + eSafeVoid,
                "Computation: " + EnumChatFormatting.GREEN + eAvailableData + EnumChatFormatting.RESET + " / " + EnumChatFormatting.YELLOW + eRequiredData + EnumChatFormatting.RESET,
                "Max Parrallel: "+ EnumChatFormatting.GREEN + getMaxParalells()
        };
    }
}
