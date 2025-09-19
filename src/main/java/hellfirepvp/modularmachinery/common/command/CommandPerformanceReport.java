package hellfirepvp.modularmachinery.common.command;

import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

import static github.kasuminova.mmce.common.concurrent.TaskExecutor.executedCount;
import static github.kasuminova.mmce.common.concurrent.TaskExecutor.taskUsedTime;
import static github.kasuminova.mmce.common.concurrent.TaskExecutor.totalExecuted;
import static github.kasuminova.mmce.common.concurrent.TaskExecutor.totalUsedTime;

public class CommandPerformanceReport extends CommandBase {
    private static final String LANG_KEY = "command.modularmachinery.performance_report";

    @Nonnull
    @Override
    public String getName() {
        return "mm-performance_report";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return LANG_KEY;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equals("reset")) {
            executedCount = 0;
            totalExecuted = 0;
            totalUsedTime = 0;
            taskUsedTime = 0;
            sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".reset"));
            return;
        }

        long executedAvgPerExecution = executedCount == 0 ? 0 : totalExecuted / executedCount;
        double usedTimeAvgPerExecution = executedCount == 0 ? 0 : (double) (totalUsedTime / executedCount) / 1000;
        double taskUsedTimeAvg = totalExecuted == 0 ? 0 : (double) (taskUsedTime / executedCount) / 1000;
        long usedTimeAvg = totalExecuted == 0 ? 0 : taskUsedTime / totalExecuted;

        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".title",
            TextFormatting.GREEN + MiscUtils.formatDecimal(executedCount) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentString(""));

        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".total_executed",
            TextFormatting.BLUE + MiscUtils.formatDecimal(totalExecuted) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".tasks_avg_per_execution",
            TextFormatting.BLUE + String.valueOf(executedAvgPerExecution) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentString(""));

        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".total_used_time",
            TextFormatting.BLUE + String.valueOf(totalUsedTime / 1000) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".used_time_avg_per_execution",
            TextFormatting.YELLOW + String.format("%.2f", usedTimeAvgPerExecution) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentString(""));

        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".task_used_time",
            TextFormatting.BLUE + MiscUtils.formatDecimal(((double) taskUsedTime / 1000L)) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentString(""));

        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".task_used_time_avg",
            TextFormatting.YELLOW + String.format("%.2f", taskUsedTimeAvg) + TextFormatting.RESET));
        sender.sendMessage(new TextComponentTranslation(LANG_KEY + ".used_time_avg",
            TextFormatting.BLUE + String.valueOf(usedTimeAvg) + TextFormatting.RESET));
    }
}
