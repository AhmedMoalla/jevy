package com.jevy.testbed;

import com.jevy.ecs.*;
import com.jevy.ecs.annotation.*;
import com.jevy.ecs.schedule.*;
import com.jevy.ecs.systemset.*;
import com.jevy.testbed.components.*;
import lombok.extern.java.*;

@Log
@ScanSystems
public class SystemSetsMain {

    public static void main(String[] args) {
        var world = new ECSWorld();
        world.addResource(new CustomResource("test"));

        Schedule schedule = Schedule.defaultSchedule();
        schedule.run(world);
    }

    static class ABCLabel implements SystemLabel {
    }

    static class MySystemSet implements SystemSetRulesProvider {
        @Override
        public SystemSetRules buildRules() {
            return SystemSetRules.builder()
                    .before("B")
                    .after("C")
                    .build();
        }
    }

    @Label(ABCLabel.class)
    @FunctionalSystem
    public void A(CustomResource resource) {
        log.entering(SystemSetsMain.class.getCanonicalName(), "A");
    }

    @Label(ABCLabel.class)
    @FunctionalSystem
    public void B(CustomResource resource) {
        log.entering(SystemSetsMain.class.getCanonicalName(), "B");
    }

    @Label(ABCLabel.class)
    @FunctionalSystem
    public void C(CustomResource resource) {
        log.entering(SystemSetsMain.class.getCanonicalName(), "C");
    }

    @Order(before = "A")
    @FunctionalSystem
    public void runsBeforeAUsingOrderSystemName(CustomResource resource) {
        log.entering(SystemSetsMain.class.getCanonicalName(), "runsBeforeAUsingOrderSystemName");
    }

    @Order(after = "A")
    @FunctionalSystem
    public void runsAfterAUsingOrderSystemName() {
        log.entering(SystemSetsMain.class.getCanonicalName(), "runsAfterAUsingOrderSystemName");
    }

    @Order(beforeLabel = ABCLabel.class)
    @FunctionalSystem
    public void runsBeforeABCUsingOrderLabel(CustomResource resource) {
        log.entering(SystemSetsMain.class.getCanonicalName(), "runsBeforeABCUsingOrderLabel");
    }

    @Order(afterLabel = ABCLabel.class)
    @FunctionalSystem
    public void runsAfterABCUsingOrderLabel() {
        log.entering(SystemSetsMain.class.getCanonicalName(), "runsAfterABCUsingOrderLabel");
    }

    @SystemSet(MySystemSet.class)
    @FunctionalSystem
    public void runBeforeBandAfterCUsingSystemSet() {
        log.entering(SystemSetsMain.class.getCanonicalName(), "runBeforeBandAfterCUsingSystemSet");
    }
}
