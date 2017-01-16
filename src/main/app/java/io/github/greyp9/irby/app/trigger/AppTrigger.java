package io.github.greyp9.irby.app.trigger;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.xed.model.Xed;
import io.github.greyp9.arwo.core.xed.trigger.XedTrigger;

public class AppTrigger implements XedTrigger {

    public void onPersist(String contextPath, Xed xed) {
        System.setProperty(App.Application.NAME, getClass().getName());
    }
}
