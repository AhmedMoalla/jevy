package com.jevy.ecs;

public interface Schedule {
    Class<? extends ScheduleLabel> label();

    void run(ECSWorld world);

    void addSystem(SystemRunner systemRunner);

    static Schedule defaultSchedule() {
        return new ScheduleImpl(DefaultScheduleLabel.class);
    }

    static Schedule fromLabel(Class<? extends ScheduleLabel> label) {
        return new ScheduleImpl(label);
    }
}