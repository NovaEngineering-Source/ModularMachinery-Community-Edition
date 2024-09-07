package github.kasuminova.mmce.common.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.util.function.BooleanSupplier;

public enum Sides {
    
    CLIENT(Sides::isRunningOnClient),
    SERVER(Sides::isRunningOnServer);

    private final BooleanSupplier precondition;

    Sides(final BooleanSupplier precondition) {
        this.precondition = precondition;
    }

    public void runIfPresent(final Runnable runnable) {
        if (precondition.getAsBoolean()) {
            runnable.run();
        }
    }

    public static void accept(final Runnable serverRunnable, final Runnable clientRunnable) {
        if (isServer()) {
            if (serverRunnable != null) {
                serverRunnable.run();
            }
        } else {
            if (clientRunnable != null) {
                clientRunnable.run();
            }
        }
    }

    public static boolean isRunningOnServer() {
        return FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    public static boolean isRunningOnClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static boolean isServer() {
        return FMLLaunchHandler.side().isServer();
    }

    public static boolean isClient() {
        return FMLLaunchHandler.side().isClient();
    }

}
