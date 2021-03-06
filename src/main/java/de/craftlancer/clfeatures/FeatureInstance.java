package de.craftlancer.clfeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import de.craftlancer.core.conversation.ClickableBooleanPrompt;
import de.craftlancer.core.conversation.FormattedConversable;
import de.craftlancer.core.structure.BlockStructure;

//TODO transfer feature ownership
public abstract class FeatureInstance implements Listener, ConfigurationSerializable {
    private UUID ownerId;
    private BlockStructure structure;
    private Location initialBlock;
    private String usedSchematic;
    
    private ConversationFactory conversation = new ConversationFactory(CLFeatures.getInstance()).withLocalEcho(false).withModality(false).withTimeout(30)
                                                                                                .withFirstPrompt(new DestroyPrompt());
    
    public FeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        this.ownerId = ownerId;
        this.structure = blocks;
        this.initialBlock = location;
        this.usedSchematic = usedSchematic;
        
        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
    }
    
    public FeatureInstance(Map<String, Object> map) {
        this.ownerId = UUID.fromString(map.get("owner").toString());
        this.initialBlock = (Location) map.get("lecternLoc");
        this.structure = (BlockStructure) map.get("structure");
        this.usedSchematic = Objects.toString(map.get("usedSchematic"), null);
        
        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
    }
    
    public String getUsedSchematic() {
        return usedSchematic;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("owner", ownerId.toString());
        map.put("structure", structure);
        map.put("lecternLoc", getInitialBlock()); // legacy name
        map.put("usedSchematic", usedSchematic);

        return map;
    }
    
    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }
    
    public boolean isOwner(UUID uuid) {
        return ownerId.equals(uuid);
    }
    
    protected abstract void tick();
    
    protected abstract Feature<? extends FeatureInstance> getManager();
    
    public void destroy() {
        HandlerList.unregisterAll(this);
        getManager().remove(this);
        
        structure.forEach(a -> a.getBlock().setType(Material.AIR));
    }
    
    public Location getInitialBlock() {
        return initialBlock.clone();
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public BlockStructure getStructure() {
        return structure;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInitialDestroy(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(initialBlock))
            return;
        
        conversation.buildConversation(new FormattedConversable(event.getPlayer())).begin();
        event.setCancelled(true);
    }
    
    private class DestroyPrompt extends ClickableBooleanPrompt {
        
        public DestroyPrompt() {
            super(CLFeatures.CC_PREFIX + "§eDo you really want to destroy this feature? It will be gone forever! Check the move command of the feature otherwise.");
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            if (input) {
                destroy();
                context.getForWhom().sendRawMessage(CLFeatures.CC_PREFIX + "§eYou destroyed this feature.");
            }
            else
                context.getForWhom().sendRawMessage(CLFeatures.CC_PREFIX + "§eYou didn't destroy this feature.");
            return END_OF_CONVERSATION;
        }
        
    }
}
