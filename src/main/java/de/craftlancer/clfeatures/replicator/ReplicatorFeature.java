package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReplicatorFeature extends Feature<ReplicatorFeatureInstance> {
    private List<ReplicatorFeatureInstance> instances;
    
    private List<Material> blockedProducts;
    
    @SuppressWarnings("unchecked")
    public ReplicatorFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "replicator.limit"));
        
        blockedProducts = config.getStringList("excludedProducts").stream().map(Material::getMaterial).collect(Collectors.toList());
        
        instances = (List<ReplicatorFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/replicator.yml"))
                                                                       .getList("replicator", new ArrayList<>());
    }
    
    public List<Material> getBlockedProducts() {
        return blockedProducts;
    }
    
    // Unused with blueprints
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return false;
    }
    
    // Unused with blueprints
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return Collections.emptyList();
    }
    
    // Unused with blueprints
    @Override
    public boolean createInstance(Player creator, Block initialBlock) {
        return false;
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/replicator.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("replicator", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            }
            catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Replicator: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new ReplicatorCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof ReplicatorFeatureInstance) {
            instances.remove(instance);
        }
    }
    
    @Nonnull
    @Override
    protected String getName() {
        return "Replicator";
    }
    
    @Override
    public List<ReplicatorFeatureInstance> getFeatures() {
        instances.removeIf(Objects::isNull);
        return instances;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialLocation, List<Location> blocks, String usedSchematic) {
        return instances.add(new ReplicatorFeatureInstance(this, creator.getUniqueId(), new BlockStructure(blocks), initialLocation.getLocation(), usedSchematic));
    }
    
    /*
     * DUPE PREVENTION
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (event.getItem().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemPortalUse(EntityPortalEvent event) {
        if (event.getEntityType() != EntityType.DROPPED_ITEM)
            return;
        
        if (event.getEntity().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (event.getEntity().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA)
                || event.getTarget().hasMetadata(ReplicatorDisplayItem.DISPLAY_ITEM_METADATA))
            event.setCancelled(true);
    }
    
}
