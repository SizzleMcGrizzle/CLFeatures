package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class ReplicatorMoveCommand extends SubCommand {
    public ReplicatorMoveCommand(Plugin plugin) {
        super("", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        Player p = (Player) sender;
        p.setMetadata(ReplicatorFeatureInstance.MOVE_METADATA, new FixedMetadataValue(getPlugin(), ""));
        
        new LambdaRunnable(() -> {
            if (!p.hasMetadata(ReplicatorFeatureInstance.MOVE_METADATA))
                return;
            
            p.removeMetadata(ReplicatorFeatureInstance.MOVE_METADATA, getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Replicator move timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Right click the replicator you want to move.";
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented        
    }
}
