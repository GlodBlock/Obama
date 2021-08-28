package com.gtnewhorizons.obama.main.tileentites.multi.definition.structure;

import com.gtnewhorizon.structurelib.util.Vec3Impl;

/**
 * <p>The simple constructable structure interface implementation used to construct and check machines.
 *
 * <p>It is meant to be used for multiblocks with just one state.
 */
public interface IConstructableStructureSimple extends IConstructableStructure {
    String TM_STRUCTURE_START = "START";

    /**
     * <p>Gets start structure offset.
     *
     * <p>Intended to be defined by the final machine class implementation.
     *
     * @return the start structure offset
     */
    Vec3Impl getStartStructureOffset();

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    default boolean checkMachine(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
//        return structureCheck(TM_STRUCTURE_START, getStartStructureOffset());
//    }

    /**
     * {@inheritDoc}
     *
     * <p>Construct the machine.
     *
     * @param itemStack the player held item stack
     * @param hintsOnly the hints only mode switch
     */
//    @Override
//    default void construct(ItemStack itemStack, boolean hintsOnly) {
//        structureBuild(TM_STRUCTURE_START, getStartStructureOffset(), hintsOnly, itemStack);
//    }
}