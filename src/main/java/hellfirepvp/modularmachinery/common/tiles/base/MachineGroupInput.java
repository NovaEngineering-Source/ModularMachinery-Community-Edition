package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;

/**
 * 实现此接口允许将输入仓室划分为“组”，不实现的情况下默认为-1组，被所有输入通用，组ID必须>0，对于不同的组，输入是互相隔离的
 * TileColorableMachineComponent实现了此接口，{@link #canGroupInput}返回false，重写返回true认为是可分配组的输入供GUI修改
 * 同时，应该重写{@link MachineComponent#getGroupID()}使其返回getGroupId的对应值
 * {@link #isGroupInput}返回false时，{@link #getGroupId()}应当返回-1
 */
public interface MachineGroupInput {

    void setGroupId(int groupId);

    boolean canGroupInput();

    boolean isGroupInput();

    void setGroupInput(boolean b);

    int getGroupId();

}