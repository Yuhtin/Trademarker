package me.jann.trademarker.commands;

import me.jann.trademarker.Trademarker;
import me.jann.trademarker.WatermarkRenderer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static me.jann.trademarker.Trademarker.*;

public class TrademarkCommand implements CommandExecutor {

    private final Trademarker main;

    public TrademarkCommand(Trademarker main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be a player to use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("trademarker.use")) {
            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.no_perms")));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().equals(Material.FILLED_MAP)) {
            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.not_holding_map")));
            return true;
        }

        if (args.length < 1) return true;

        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null) {
            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.not_holding_map")));
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!isTrademarkedMap(item)) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.remove_no_trademark")));
                return false;
            }

            if (!isMapOwner(player.getUniqueId(), item) && !player.hasPermission("trademarker.remove.other")) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.cant_remove")));
                return false;
            }

            meta.setLore(new ArrayList<>());
            meta.getPersistentDataContainer().remove(TRADEMARK_OWNER_KEY);
            item.setItemMeta(meta);

            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.trademark_removed")));
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (isTrademarkedMap(item)) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.cant_trademark")));
                return false;
            }

            List<String> lore = new ArrayList<>();

            String trademark = Trademarker.colorCode(main.getConfig()
                    .getString("lang.trademark_format")
                    .replace("%player%", player.getName())
            );

            lore.add(trademark);
            meta.setLore(lore);

            String uuid = player.getUniqueId().toString();
            meta.getPersistentDataContainer().set(TRADEMARK_OWNER_KEY, PersistentDataType.STRING, uuid);

            item.setItemMeta(meta);

            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.trademark_added")));
            return true;
        }

        if (args[0].equalsIgnoreCase("watermark")) {
            if (!player.hasPermission("trademarker.watermark")) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.no_perms")));
                return true;
            }

            if (!isTrademarkedMap(item)) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.watermark_no_trademark")));
                return true;
            }

            if (!isMapOwner(player.getUniqueId(), item) && !player.hasPermission("trademarker.watermark.others")) {
                player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.watermark_others_trademarked")));
                return false;
            }

            MapView view = meta.getMapView();
            String posx = "";
            String posy = "";

            if (args.length > 1) {
                posy = args[1];
                if (args.length > 2) {
                    posx = args[2];
                }
            }

            view.addRenderer(new WatermarkRenderer(player.getName(), posx, posy));

            player.sendMessage(Trademarker.colorCode(main.getConfig().getString("lang.watermark_added")));

            return true;
        }

        return false;
    }

}