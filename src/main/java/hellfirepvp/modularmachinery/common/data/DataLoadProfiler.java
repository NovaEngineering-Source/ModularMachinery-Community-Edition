/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataLoadProfiler
 * Created by HellFirePvP
 * Date: 16.08.2017 / 15:21
 */
public class DataLoadProfiler {

    private final List<StatusLine> statusLines = new LinkedList<>();

    public StatusLine createLine(String name) {
        StatusLine sl = new StatusLine(name);
        statusLines.add(sl);
        return sl;
    }

    public void printLines(@Nullable ICommandSender additionalReceiver) {
        for (StatusLine sl : statusLines) {
            String out = sl.toString();
            ModularMachinery.log.info(out);
            if (additionalReceiver != null) {
                additionalReceiver.sendMessage(new TextComponentString(out));
            }
        }
    }

    public static class StatusLine {
        private final List<Status> lineComponents = new LinkedList<>();
        private final String       name;

        public StatusLine(String name) {
            this.name = name;
        }

        public Status appendStatus(String regexName) {
            Status status = new Status(regexName);
            lineComponents.add(status);
            return status;
        }

        @Override
        public String toString() {
            return name + Arrays.toString(lineComponents.toArray());
        }
    }

    public static class Status {

        private final String name;
        private       int    counter = 0;

        public Status(String name) {
            this.name = name;
        }

        public void incrementCounter() {
            counter++;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public String toString() {
            return String.format(name, counter);
        }
    }

}
