package com.xxyrana.blockcombiner;

import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author xxyrana
 *
 */
public class BlockCombiner extends JavaPlugin implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Permission perms = null;
	private static HashMap<String, String> materials = new HashMap<String, String>();
	private static HashMap<String, Byte> mdata = new HashMap<String, Byte>();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		setupPermissions();
		setupMaterials();
	}

	@Override
	public void onDisable() {
		log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("block") && sender instanceof Player && perms.has(sender, "blocks.block")) {
			Player player = (Player) sender;
			if(args.length < 1){ //combine all ores (default)
				replace(player, "lapis", "22", 9, "lapis", "lapis block(s)");
				replace(player, "redstone", "152", 9, "redstone", "redstone blocks(s)");
				replace(player, "coal", "173", 9, "coal", "coal block(s)");
				replace(player, "265", "42", 9, "iron ingots", "iron block(s)");
				replace(player, "266", "41", 9, "gold ingots", "gold block(s)");
				replace(player, "diamond", "57", 9, "diamonds", "diamond block(s)");
				replace(player, "emerald", "133", 9, "emeralds", "emerald block(s)");
				replace(player, "quartz", "155", 4, "quartz", "quartz block(s)");
				return true;
			}
			
			String arg = args[0];
			if(arg.equalsIgnoreCase("lapis")){
				replace(player, "lapis", "22", 9, "lapis", "lapis block(s)");
			} else if(arg.equalsIgnoreCase("redstone")){
				replace(player, "redstone", "152", 9, "redstone", "redstone blocks(s)");
			} else if(arg.equalsIgnoreCase("coal")){
				replace(player, "coal", "173", 9, "coal", "coal block(s)");
			} else if(arg.equalsIgnoreCase("iron")){
				replace(player, "265", "42", 9, "iron ingots", "iron block(s)");
			} else if(arg.equalsIgnoreCase("gold")){
				replace(player, "266", "41", 9, "gold ingots", "gold block(s)");
			} else if(arg.equalsIgnoreCase("diamond")){
				replace(player, "diamond", "57", 9, "diamonds", "diamond block(s)");
			} else if(arg.equalsIgnoreCase("emerald")){
				replace(player, "emerald", "133", 9, "emeralds", "emerald block(s)");
			} else if(arg.equalsIgnoreCase("quartz")){
				replace(player, "quartz", "155", 4, "quartz", "quartz block(s)");
			} else if(arg.equalsIgnoreCase("brick") || arg.equalsIgnoreCase("bricks")){
				replace(player, "336", "45", 4, "bricks", "brick block(s)");
				replace(player, "405", "112", 4, "nether bricks", "nether brick block(s)");
			} else if(arg.equalsIgnoreCase("wool") || arg.equalsIgnoreCase("string")){
				replace(player, "string", "wool", 4, "string", "wool");
			} else if(arg.equalsIgnoreCase("snow")){
				replace(player, "332", "80", 4, "snow balls", "snow block(s)");
			} else if(arg.equalsIgnoreCase("clay")){
				replace(player, "337", "82", 4, "clay", "clay block(s)");
			} else if(arg.equalsIgnoreCase("wheat") || arg.equalsIgnoreCase("hay")){
				replace(player, "wheat", "hay", 9, "wheat", "block(s) of hay");
			} else if(arg.equalsIgnoreCase("melon")){
				replace(player, "360", "103", 9, "melon slices", "melon(s)");
			} else {
				player.sendMessage("§7Valid arguments include individual ore names, brick, wool, snow, clay, wheat, or melon.");
			}
			player.updateInventory();
			return true;
		}
		return false; 
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	//---------------//
	// Replace Items //
	//---------------//
	private void replace(Player player, String item, String block, int num, String itemstr, String blockstr){
		//Remove items
		int i = getAmount(player, item);
		ItemStack is = getItemStack(item, i);
		player.getInventory().removeItem(is);
		player.updateInventory();

		//Make sure the correct items were taken (in case of name errors)
		if(getAmount(player, item) == 0){
			//Add blocks & leftover items
			int itemnum = i%num;
			if(itemnum > 0){
				ItemStack is2 = getItemStack(item, itemnum);
				player.getInventory().addItem(is2);
			}
			int blocknum = (int) Math.floor(i/num);
			if(blocknum > 0){
				ItemStack is3 = getItemStack(block, blocknum);
				player.getInventory().addItem(is3);
				player.sendMessage("§7" + blocknum * num + " " + itemstr +" converted to " + blocknum + " " + blockstr + ".");
			}
			player.updateInventory();
		}
	}

	//-------------//
	// Item Lookup //
	//-------------//
	private int getAmount(Player player, String sItem)
	{
		PlayerInventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
		int has = 0;
		for (ItemStack item : items)
		{
			if ((item != null) && (item.getType().equals(getMaterial(sItem))) && (item.getAmount() > 0)
					&& (item.getDurability() == getData(sItem)))
			{
				has += item.getAmount();
			}
		}
		return has;
	}
	private ItemStack getItemStack(String str, int num){
		Material m;
		//Check for id formatting
		if(str.contains(":")){
			String[] ids = str.split(":");
			m = Material.matchMaterial(ids[0]);
			ItemStack i = new ItemStack(m, num);
			i.setDurability(Byte.parseByte(ids[1]));
			return i;
		}
		//Check built in material function
		m = Material.matchMaterial(str);
		if(m!=null){
			ItemStack i = new ItemStack(m, num);
			return i;
		}
		//Check custom aliases
		String key = str.toLowerCase();
		if(key.contains(" ")){
			key = key.replace(" ", "");
		}
		if(materials.containsKey(key)){
			m = Material.matchMaterial(materials.get(key));
			if(m!=null){
				ItemStack i = new ItemStack(m, num);
				if(mdata.containsKey(key)){
					i.setDurability(mdata.get(key));
				}
				return i;
			}
		}
		//Does not exist or not accounted for
		return null;
	}
	private Material getMaterial(String str){
		Material m;
		//Check for id formatting
		if(str.contains(":")){
			String[] ids = str.split(":");
			m = Material.matchMaterial(ids[0]);
			return m;
		}
		//check built in material function
		m = Material.matchMaterial(str);
		if(m!=null){
			return m;
		}
		//Check custom aliases
		String key = str.toLowerCase();
		if(key.contains(" ")){
			key = key.replace(" ", "");
		}
		if(materials.containsKey(key)){
			m = Material.matchMaterial(materials.get(key));
			return m;
		}
		//Does not exist or not accounted for
		return null;
	}

	private short getData(String str){
		if(str != null){
			String key = str.toLowerCase();
			if(key.contains(" ")){
				key = key.replace(" ", "");
			}
			if(mdata!=null && mdata.containsKey(key)){
				return mdata.get(key);
			}
		}
		return (byte) 0;
	}

	private void setupMaterials(){
		byte i=0;

		materials.put("quartzore", "153");
		materials.put("haybale", "hay_block");
		materials.put("hay", "hay_block");
		materials.put("slimeball", "slime ball");


		materials.put("lapislazuli", "ink_sack");
		materials.put("lapis", "ink_sack");	
		i = 4;
		mdata.put("lapislazuli", i);
		mdata.put("lapis", i);
	}
}
