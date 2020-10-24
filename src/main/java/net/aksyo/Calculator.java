package net.aksyo;

import net.aksyo.utils.GUI;
import net.aksyo.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

public class Calculator {

    private GUI calculator; //The Calculator Visual Interface (Minecraft Inventory)
    private Player player; //The player that is using the calculator
    private ItemStack screen = new ItemStack(Material.PAINTING); //The item that represents the screen (shows the operation)
    private ItemMeta meta = screen.getItemMeta(); //The meta of the item used to update the screen
    private char[] operators = { '+', '-', 'x', '/' }; //The operators for the calculator

    public Calculator(Player player) {

        this.player = player;
        //Creates a new GUI of 8 rows (72 slots)
        calculator = new GUI(Main.getInstance(), "§2Texas Instrument", 8);

        //Setting up all the items in the GUI

        setScreenMeta();
        setNumbers(2);
        setOperators(2);
        setSpecialButtons();
        setDecoration((short) 5);

        //GUI attributes (all slots locked) & opening the gui to the player
        calculator.setLocked(true);
        calculator.open(player);

    }

    /**
     * Set the meta of the screen item (Display name, Lore & Action if clicked)
     */
    private void setScreenMeta() {
        meta.setDisplayName("§3§lCurrent Operation");
        meta.setLore(Arrays.asList("§6------------------------------",
                " ",
                "§6------------------------------"));
        screen.setItemMeta(meta);
        calculator.setItem(13, screen, ((target, inventoryClickEvent) -> {
            showChat(5);
        }));
    }

    /**
     * Set all the digits into a 3x3 square
     * @param line The line of the gui to form the square (add 1 to the line to get the correct line)
     */
    private void setNumbers(int line) {
        //Loops 9 times
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0) line++; //If i is divisible by 3, then we switch lines
            int slot = line * 9 + i % 3, num = i + 1; //Slot : line * 9 (to get slot of the line), then add i divisible by 3    Num (digit) : i + 1 (looping from 0 to 8)
            calculator.setItem(slot, new ItemBuilder(Material.ICE).setName("§6" + num).create(), (target, inventoryClickEvent) -> {
                //If the digit is the first of the operation, then we include a space in front of it
                String value = calculator.getInventory().getName().toCharArray().length == 1 ?  " " + num : String.valueOf(num),
                        current = meta.getLore().get(1);
                updateScreen(value, current);
            });
        }

        calculator.setItem(54, new ItemBuilder(Material.PACKED_ICE).setName("§60").create(), (target, inventoryClickEvent) -> {
            //If the digit is the first of the operation, then we include a space in front of it
            String value = calculator.getInventory().getName().toCharArray().length == 1 ?  " " + 0 : "0",
                    current = meta.getLore().get(1);
            updateScreen(value, current);
        });
        calculator.setItem(55, new ItemBuilder(Material.PACKED_ICE).setName("§6.").create(), (target, inventoryClickEvent) -> {
            String value = ".",
                    current = meta.getLore().get(1);
            //If there isn't a digit in front then we cannot place a '.' in the operation
            if (canInsertOperator(meta.getLore().get(1).toCharArray())) {
                updateScreen(value, current);
            } else {
                player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
            }

        });
    }

    /**
     * Set all the operators in a 2x2 square
     * @param line The line of the gui to form the square (add 1 to the line to get the correct line)
     */
    private void setOperators(int line) {
        int opLine = 4; //opLine is used to add 4 or 5 to the slot because slot basic calculation will return the first slot of the line
        //Looping n times, with n being the length of the operators array
        for (int i = 0; i < operators.length; i++, opLine++) {
            if (i % 2 == 0) line++; //If i divisible by 2, the we switch lines
            if (opLine == 5) opLine = 4; //Changes back to 4 whenever we switch lines
            int slot = line * 9 + i % 2 + opLine; //Slot : line * 9 + i divisible by 2 + (4 ? 5)
            char op = operators[i]; //Fetching the operator in the array
            calculator.setItem(slot, new ItemBuilder(Material.IRON_BLOCK).setName("§6" + op).create(), (target, inventoryClickEvent) -> {
                //Putting spaces between the number in front and behind
                String value = " " + String.valueOf(op) + " ",
                        current = meta.getLore().get(1);
                //If there isn't a digit in front then we cannot place an operator in the operation, except if the operator is '-' to have negative numbers.
                if (canInsertOperator(meta.getLore().get(1).toCharArray()) || op == '-') {
                    updateScreen(value, current);
                } else {
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
                }
            });
        }
    }

    /**
     * Adds the calculation item and the square item (not done yet)
     */
    private void setSpecialButtons() {
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
                Object result = calculate(meta.getLore().get(1));
                player.sendMessage("§aThe result is : §b" + result);
                playSound();
            } catch (ScriptException e) {
                player.sendMessage("§cError : Math Exception");
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);

                e.printStackTrace();
            }
        }));
    }

    /**
     * Set stained glass pane for decoration
     * @param color The color of the stained glass pane
     */
    private void setDecoration(short color) {
        //Loops 9 times to complete 1 line
        for (int i = 0; i < 9; i++) {
            //Placing the decoration on line (18 / 9) = 2
            calculator.setItem(18 + i, new ItemStack(Material.STAINED_GLASS_PANE, 1, color), ((target, inventoryClickEvent) -> {
                showChat(2);
            }));
            //Placing the decoration on line (63 / 9) = 7
            calculator.setItem(63 + i, new ItemStack(Material.STAINED_GLASS_PANE, 1, color), ((target, inventoryClickEvent) -> {
                showChat(2);
            }));
        }
    }

    /**
     * Check if we are allowed to place an operator
     * @param arr The operation char array
     * @return If the last char of the array is a digit and if the length of the array is superior to 1
     */
    protected boolean canInsertOperator(char[] arr) {
        return Character.isDigit(arr[arr.length - 1]) && arr.length > 1;
    }

    /**
     * Updates the screen item meta, to put the new operation (changes the lore)
     * @param value The value we wish to insert into the operation
     * @param current The line of the lore we wish to change
     */
    protected void updateScreen(String value, String current) {
        calculator.getInventory().remove(screen); //Removes the current screen for the GUI
        current += value; //Adds the value to the current operation
        List<String> lore = meta.getLore(); //Get items lore
        lore.set(1, current); //Set the new operation to the second line
        meta.setLore(lore);
        screen.setItemMeta(meta); //Sets the new meta
        //Action if clicked
        calculator.setItem(13, screen, ((target, inventoryClickEvent) -> {
            showChat(5);
        }));
    }

    /**
     * Shows the current operation in chat to the player, then reopens the calculator GUI
     * @param delayInSeconds the delay before reopening the GUI
     */
    protected void showChat(int delayInSeconds) {
        player.closeInventory();
        player.sendMessage("§3Current : §c" + screen.getItemMeta().getLore().get(1));
        new BukkitRunnable() {
            @Override
            public void run() {
                calculator.open(player);
            }
        }.runTaskLater(Main.getInstance(), delayInSeconds * 20);
    }

    /**
     * Play a cool piano sound
     */
    protected void playSound() {
        for (int i = 0; i < 5; i++) {
            final float volume = 0.5f + i * 0.2f;
            final int ticks = i * 4;
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, volume);
                }
            }.runTaskLater(Main.getInstance(), ticks);
        }

    }

    /**
     *
     * @param operation The operation we wish to eval (String)
     * @return Returns the result of our operation (can be a double or an integer)
     * @throws ScriptException If the operation cannot be executed due to an invalid operation
     */
    protected final Object calculate(String operation) throws ScriptException {
        //Removes the spaces in the operation and changes the 'x' (times) into '*'
        final String finalOperation = operation.replaceAll(" ", "").replaceAll("x", "*");
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        return engine.eval(finalOperation);
    }
}
