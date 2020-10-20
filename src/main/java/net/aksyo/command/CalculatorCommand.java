package net.aksyo.command;

import net.aksyo.Main;
import net.aksyo.utils.GUI;
import net.aksyo.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class CalculatorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;
            new Calculate(player);
        }

        return false;
    }


}


class Calculate {


    private GUI calculator;
    private Player player;
    private ItemStack screen = new ItemStack(Material.RAILS);
    private ItemMeta meta = screen.getItemMeta();
    private char[] operators = { '+', '-', 'x', '/' };

    public Calculate(Player player) {

        this.player = player;
        calculator = new GUI(Main.getInstance(), "§2Texas Instrument", 8);

        meta.setDisplayName(" ");
        screen.setItemMeta(meta);
        calculator.setItem(4, screen, ((target, inventoryClickEvent) -> {
            showChat();
        }));

        int skip = 2, t = 4;
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0) skip++;
            int slot = skip * 9 + i % 3, num = i + 1;
            calculator.setItem(slot, new ItemBuilder(Material.ICE).setName("§6" + num).create(), (target, inventoryClickEvent) -> {
                String value = calculator.getInventory().getName().toCharArray().length != 1 ?  " " + num : String.valueOf(num),
                        current = meta.getDisplayName();
                updateScreen(value, current);
            });

        }

        skip = 2;
        for (int i = 0; i < operators.length; i++, t++) {
            if (i % 2 == 0) skip++;
            if (t == 5) t = 4;
            int slot = skip * 9 + i % 2 + t;
            char op = operators[i];
            calculator.setItem(slot, new ItemBuilder(Material.IRON_BLOCK).setName("§6" + op).create(), (target, inventoryClickEvent) -> {
                String value = " " + String.valueOf(op) + " ",
                        current = meta.getDisplayName();
                if (canInsertOperator(meta.getDisplayName().toCharArray()) || op == '-') {
                    updateScreen(value, current);
                } else {
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
                }
            });
        }

        calculator.setItem(35, new ItemBuilder(Material.GOLD_AXE).setName("§3Calculate").create(), ((target, inventoryClickEvent) -> {
            player.closeInventory();
            try {
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
                Object result = calculate(meta.getDisplayName());
                player.sendMessage("§aThe result is : §b" + result);
            } catch (ScriptException e) {
                player.sendMessage("§cError : Math Exception");
                e.printStackTrace();
            }
        }));

        calculator.setLocked(true);
        calculator.open(player);

    }

    protected boolean canInsertOperator(char[] arr) {
        return Character.isDigit(arr[arr.length - 1]) && arr.length > 1;
    }

    protected void updateScreen(String value, String current) {
        calculator.getInventory().remove(screen);
        System.out.println("Value : " + value + " Current : " + current);
        current = current + value;
        meta.setDisplayName(current);
        screen.setItemMeta(meta);
        calculator.setItem(4, screen, ((target, inventoryClickEvent) -> {
            showChat();
        }));
    }

    protected void showChat() {
        player.closeInventory();
        player.sendMessage("§3Current : §c" + screen.getItemMeta().getDisplayName());
        new BukkitRunnable() {
            @Override
            public void run() {
                calculator.open(player);
            }
        }.runTaskLater(Main.getInstance(), 100);
    }

    protected final Object calculate(String operation) throws ScriptException {
        final String finalOperation = operation.replaceAll(" ", "");
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        return engine.eval(operation);
    }


}
