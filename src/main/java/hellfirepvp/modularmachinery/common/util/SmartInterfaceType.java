package hellfirepvp.modularmachinery.common.util;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.SmartInterfaceType")
public class SmartInterfaceType implements Comparable<SmartInterfaceType> {
    //智能数据接口的类型，在一个机械内应当是独一无二的
    private final String type;
    //智能数据接口的默认数值
    private final float  defaultValue;
    //智能数据接口的 GUI 显示信息（在数值信息上方）（若为空则使用默认信息）
    private       String headerInfo          = "";
    //智能数据接口的 GUI 数值显示信息（若为空则使用默认信息）
    private       String valueInfo           = "";
    //智能数据接口的 GUI 显示信息（在数值信息下方）（若为空则使用默认信息）
    private       String footerInfo          = "";
    //当控制器检查配方不匹配数值时显示的信息（若为空则使用默认信息）
    private       String notEqualMessage     = "";
    //JEI 配方中提示的信息（若为空则使用默认信息）
    private       String jeiTooltip          = "";
    /**
     * <p>使用自定义 JEI 信息显示时，args 的数量（***非常重要！必须与上方提示信息中的 args 数量相同。***）</p>
     * <p>例：`配方模式：%s` 的 args 是一个。</p>
     * <p>例：`最低超频速率：%.1f，最高超频速率：%.1f` 的 args 是两个。</p>
     */
    private       int    jeiTooltipArgsCount = 2;
    /**
     * <p>优先级</p>
     * <p>当控制器结构的智能数据接口数量超过机械中预定义的智能数据接口类型时，新加入的智能接口会优先使用优先级最高的智能数据接口类型。</p>
     * <p>例：类型 A 的优先级是 100，类型 B 的优先级是 1000，那么当结构中的智能数据接口数量超过机器定义的类型数量时会使用类型 B 给新的智能数据接口。</p>
     */
    private       int    priority            = 0;

    public SmartInterfaceType(String type, float defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @ZenMethod
    public static SmartInterfaceType create(String type, float defaultValue) {
        return new SmartInterfaceType(type, defaultValue);
    }

    @ZenGetter("type")
    public String getType() {
        return type;
    }

    @ZenGetter("defaultValue")
    public float getDefaultValue() {
        return defaultValue;
    }

    @ZenGetter("headerInfo")
    public String getHeaderInfo() {
        return headerInfo;
    }

    @ZenMethod
    public SmartInterfaceType setHeaderInfo(String headerInfo) {
        this.headerInfo = headerInfo;
        return this;
    }

    @ZenGetter("valueInfo")
    public String getValueInfo() {
        return valueInfo;
    }

    @ZenMethod
    public SmartInterfaceType setValueInfo(String valueInfo) {
        this.valueInfo = valueInfo;
        return this;
    }

    @ZenGetter("footerInfo")
    public String getFooterInfo() {
        return footerInfo;
    }

    @ZenMethod
    public SmartInterfaceType setFooterInfo(String footerInfo) {
        this.footerInfo = footerInfo;
        return this;
    }

    @ZenGetter("notEqualMessage")
    public String getNotEqualMessage() {
        return notEqualMessage;
    }

    @ZenMethod
    public SmartInterfaceType setNotEqualMessage(String notEqualMessage) {
        this.notEqualMessage = notEqualMessage;
        return this;
    }

    @ZenGetter("jeiTooltip")
    public String getJeiTooltip() {
        return jeiTooltip;
    }

    @ZenGetter("jeiTooltipArgsCount")
    public int getJeiTooltipArgsCount() {
        return jeiTooltipArgsCount;
    }

    @ZenGetter("priority")
    public int getPriority() {
        return priority;
    }

    @ZenMethod
    public SmartInterfaceType setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @ZenMethod
    public SmartInterfaceType setJeiTooltip(String jeiTooltip, int argsCount) {
        this.jeiTooltip = jeiTooltip;
        this.jeiTooltipArgsCount = argsCount;
        return this;
    }

    @Override
    public int compareTo(SmartInterfaceType another) {
        return another.priority - priority;
    }
}
