package kport.gugu_utils.common.requirements.basic;

public interface IConsumable<T> {
    boolean consume(T outputToken, boolean doOperation);
}
