package com.jevy.testbed;

import com.jevy.ecs.annotation.*;
import com.jevy.ecs.systemset.*;

@ScanSystems
public class SystemSetsMain {

    public static void main(String[] args) {

    }


    static class MyLabel implements SystemLabel {
    }

    static class MySystemSet implements SystemSetRulesProvider {
        @Override
        public SystemSetRules buildRules() {
            return SystemSetRules.builder()
                    .after("test")
                    .before(MyLabel.class)
                    .build();
        }
    }

}
