package kport.gugu_utils.common.requirements.basic;

public interface IGeneratable<T> {
    boolean generate(T outputToken, boolean doOperation);
}
