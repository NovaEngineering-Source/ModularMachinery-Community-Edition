# Dragon Research Fusion Crafting Recipe Adapter

这是一个为 Modular Machinery Community Edition MOD 创建的配方适配器，用于适配龙之研究(Dragon Research)MOD中的聚合合成配方。

## 功能

- 将龙之研究MOD的聚合合成配方转换为模块化机械的机器配方
- 支持配方修改器(Recipe Modifiers)
- 支持额外需求和事件处理器
- 自动计算处理时间和能源消耗

## 实现指南

此适配器提供了一个基础框架，需要根据龙之研究MOD的实际API进行定制化实现。

### 步骤 1: 添加龙之研究MOD依赖

在 `build.gradle` 文件中添加龙之研究MOD作为依赖：

```gradle
dependencies {
    // 添加龙之研究MOD依赖（示例）
    implementation "com.example:dragonresearch:版本号"
    // 或者使用 compileOnly 如果只在编译时需要
    compileOnly "com.example:dragonresearch:版本号"
}
```

### 步骤 2: 实现配方获取方法

在 `AdapterDragonResearchFusionCrafting.java` 文件中，找到 `getFusionCraftingRecipes()` 方法并实现实际的配方获取逻辑：

```java
private List<Object> getFusionCraftingRecipes() {
    List<Object> recipes = new ArrayList<>();
    
    try {
        // 根据龙之研究MOD的实际API实现
        // 例如：
        // recipes = DragonResearchAPI.getFusionRecipes();
        // 或者：
        // IRecipeManager manager = DragonResearchAPI.getRecipeManager();
        // recipes = manager.getFusionCraftingRecipes();
        
    } catch (Exception e) {
        System.err.println("Error loading Dragon Research fusion recipes: " + e.getMessage());
    }
    
    return recipes;
}
```

### 步骤 3: 实现配方转换方法

在 `convertFusionRecipe()` 方法中实现具体的配方转换逻辑：

```java
private MachineRecipe convertFusionRecipe(Object fusionRecipe, ...) {
    // 转换龙之研究的聚合合成配方到模块化机械配方
    
    // 1. 获取配方组件
    FusionCraftingRecipe drRecipe = (FusionCraftingRecipe) fusionRecipe;
    ItemStack centerItem = drRecipe.getCenterItem();
    List<ItemStack> surroundingItems = drRecipe.getSurroundingItems();
    ItemStack output = drRecipe.getOutput();
    
    // 2. 创建模块化机械配方
    MachineRecipe recipe = createRecipeShell(...);
    
    // 3. 添加输入输出需求
    recipe.addRequirement(new RequirementItem(IOType.INPUT, centerItem));
    for (ItemStack item : surroundingItems) {
        recipe.addRequirement(new RequirementItem(IOType.INPUT, item));
    }
    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, output));
    
    return recipe;
}
```

## 配置参数

您可以调整以下参数来适配具体的游戏平衡：

```java
public static final int BASE_WORK_TIME = 200;      // 基础处理时间(tick)
public static final int BASE_ENERGY_COST = 50000;  // 基础能源消耗
```

## 使用方法

1. 确保龙之研究MOD已安装并加载
2. 实现上述步骤中的方法
3. 重新编译和运行游戏
4. 适配器将自动注册并转换龙之研究的聚合合成配方

## 注意事项

- 此适配器需要龙之研究MOD的具体API信息才能完全实现
- 请根据龙之研究MOD的实际版本和API调整代码
- 建议在实现前先查看龙之研究MOD的源代码或文档

## 故障排除

如果遇到问题，请检查：

1. 龙之研究MOD是否正确安装
2. 依赖是否正确配置
3. 配方获取和转换逻辑是否正确实现
4. 控制台中是否有相关错误信息

## 扩展

此适配器可以进一步扩展以支持：

- 多种类型的聚合合成配方
- 特殊的材料需求
- 自定义的处理逻辑
- 与其他MOD的兼容性

---

*此适配器是为 Modular Machinery Community Edition 创建的开源项目的一部分。*
