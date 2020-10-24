package net.aksyo.command;

import net.aksyo.Calculator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class CalculatorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (args.length != 0) {
                player.sendMessage("§c/calculate");
                return true;
            }

            new Calculator(player); //New instance of the calculator class
        }

        return false;
    }

}

