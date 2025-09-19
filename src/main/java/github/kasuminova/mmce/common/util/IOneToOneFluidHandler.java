package github.kasuminova.mmce.common.util;

/**
 * Implement this interface for a multi-fluid tank that only allows one slot per type of fluid.
 */
public interface IOneToOneFluidHandler {

    /**
     * Returns whether distinct slots map to distinct types of fluids.
     *
     * @return true if the mapping is one-to-one
     */
    boolean isOneFluidOneSlot();

}
