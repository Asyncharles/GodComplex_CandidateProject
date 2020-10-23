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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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

        meta.setDisplayName("§3§lCurrent Operation");
        meta.setLore(Arrays.asList("§6------------------------------",
                " ",
                "§6------------------------------"));
        screen.setItemMeta(meta);
        calculator.setItem(13, screen, ((target, inventoryClickEvent) -> {
            showChat(5);
        }));

        int line = 2, t = 4;
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0) line++;
            int slot = line * 9 + i % 3, num = i + 1;
            calculator.setItem(slot, new ItemBuilder(Material.ICE).setName("§6" + num).create(), (target, inventoryClickEvent) -> {
                String value = calculator.getInventory().getName().toCharArray().length == 1 ?  " " + num : String.valueOf(num),
                        current = meta.getLore().get(1);
                updateScreen(value, current);
            });

        }

        calculator.setItem(54, new ItemBuilder(Material.PACKED_ICE).setName("§60").create(), (target, inventoryClickEvent) -> {
            String value = calculator.getInventory().getName().toCharArray().length == 1 ?  " " + 0 : "0",
                    current = meta.getLore().get(1);
            updateScreen(value, current);
        });

        line = 2;
        for (int i = 0; i < operators.length; i++, t++) {
            if (i % 2 == 0) line++;
            if (t == 5) t = 4;
            int slot = line * 9 + i % 2 + t;
            char op = operators[i];
            calculator.setItem(slot, new ItemBuilder(Material.IRON_BLOCK).setName("§6" + op).create(), (target, inventoryClickEvent) -> {
                String value = " " + String.valueOf(op) + " ",
                        current = meta.getLore().get(1);
                if (canInsertOperator(meta.getLore().get(1).toCharArray()) || op == '-') {
                    updateScreen(value, current);
                } else {
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
                }
            });
        }

        calculator.setItem(35, new ItemBuilder(Material.ENCHANTED_BOOK).setName("§6Square").create(), ((target, inventoryClickEvent) -> {
            String value = "²", current = meta.getDisplayName();
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
            /*
            * STILL WORKING ON THE SQUARE
             */
        }));

        calculator.setItem(44, new ItemBuilder(Material.GOLD_AXE).setName("§3Calculate").create(), ((target, inventoryClickEvent) -> {
            player.closeInventory();
            try {
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
                Object result = calculate(meta.getLore().get(1));
                player.sendMessage("§aThe result is : §b" + result);
            } catch (ScriptException e) {
                player.sendMessage("§cError : Math Exception");
                e.printStackTrace();
            }
        }));

        for (int i = 0; i < 9; i++) {
            calculator.setItem(18 + i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4), ((target, inventoryClickEvent) -> {
                showChat(2);
            }));
            calculator.setItem(63 + i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4), ((target, inventoryClickEvent) -> {
                showChat(2);
            }));
        }

        calculator.setLocked(true);
        calculator.open(player);

    }

    protected boolean canInsertOperator(char[] arr) {
        return Character.isDigit(arr[arr.length - 1]) && arr.length > 1;
    }

    protected void updateScreen(String value, String current) {
        calculator.getInventory().remove(screen);
        current = current + value;
        List<String> lore = meta.getLore();
        lore.set(1, current);
        meta.setLore(lore);
        screen.setItemMeta(meta);
        calculator.setItem(13, screen, ((target, inventoryClickEvent) -> {
            showChat(5);
        }));
    }

    protected void showChat(int delayInSeconds) {
        player.closeInventory();
        player.sendMessage("§3Current : §c" + screen.getItemMeta().getDisplayName());
        new BukkitRunnable() {
            @Override
            public void run() {
                calculator.open(player);
            }
        }.runTaskLater(Main.getInstance(), delayInSeconds * 20);
    }

    protected final Object calculate(String operation) throws ScriptException {
        final String finalOperation = operation.replaceAll(" ", "").replaceAll("x", "*");
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        return engine.eval(finalOperation);
    }


}
