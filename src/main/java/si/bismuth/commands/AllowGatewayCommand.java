package si.bismuth.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.LiteralText;
import si.bismuth.BismuthServer;

public class AllowGatewayCommand extends AbstractCommand {
	public static boolean canEnterPortal = false;

	@Override
	public String getName() {
		return "allowgateway";
	}

	@Override
	public String getUsage(CommandSource source) {
		return this.getName();
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) {
		BismuthServer.log.info("{} {} gateway generation", source.getName(), canEnterPortal ? "enabled" : "disabled");

		canEnterPortal = !canEnterPortal;
		if (canEnterPortal) {
			source.sendMessage(new LiteralText("You may now enter an ungenerated gateway near spawn."
					+ " Will automatically disable itself once a player goes through such a portal."
					+ " Run command again to toggle off."));
		} else {
			source.sendMessage(new LiteralText("Blocked ungenerated gateways near spawn."));
		}
	}
}
