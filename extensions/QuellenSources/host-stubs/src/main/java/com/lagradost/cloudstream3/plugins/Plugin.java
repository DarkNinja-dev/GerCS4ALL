package com.lagradost.cloudstream3.plugins;

import android.content.Context;
import android.content.res.Resources;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Compile-only API header for the Android host's Plugin class.
 *
 * This class is deliberately put in a separate, compile-only JAR.  It is never
 * bundled into a .cs3; CloudStream supplies the real implementation at runtime.
 */
public abstract class Plugin extends BasePlugin {
    public void load(Context context) throws Throwable {
        load();
    }

    private Resources resources;
    private Function1<? super Context, Unit> openSettings;

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Function1<? super Context, Unit> getOpenSettings() {
        return openSettings;
    }

    public void setOpenSettings(Function1<? super Context, Unit> openSettings) {
        this.openSettings = openSettings;
    }
}
