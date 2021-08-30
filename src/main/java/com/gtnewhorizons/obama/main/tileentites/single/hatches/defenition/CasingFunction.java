package com.gtnewhorizons.obama.main.tileentites.single.hatches.defenition;

import static com.gtnewhorizons.obama.main.CommonValues.MACHINE_PREFIXES;

public enum CasingFunction {
    GRINDING, PISTON, MOTOR, CIRCUIT, CONVEYOR, PUMP, EMITTER, ROTOR, ROBOT_ARM, WIRE, FILTER;

    public String getLocalizedName(int aTier) {
        String name = this.name();
        name = name.charAt(0) + name.substring(1).toLowerCase();
        String prefix = MACHINE_PREFIXES[aTier];
        return prefix + " " + name + " Casing";
    }

    public String getUnlocalizedName(int aTier) {
        String name = this.name();
        return "gt.blockmachines.multimachine.tm." + name.toLowerCase() + "_casing." + aTier + ".name";
    }
}