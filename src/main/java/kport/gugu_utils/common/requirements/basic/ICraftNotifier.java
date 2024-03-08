package kport.gugu_utils.common.requirements.basic;

public interface ICraftNotifier<T extends IResourceToken> {
    default void startCrafting(T outputToken) {

    }
    default void finishCrafting(T outputToken) {

    }

}
