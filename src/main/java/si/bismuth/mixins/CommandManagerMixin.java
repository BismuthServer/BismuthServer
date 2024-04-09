package si.bismuth.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.handler.CommandListener;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.handler.CommandRegistry;
import net.minecraft.server.command.source.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.commands.*;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin extends CommandRegistry implements CommandListener {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCtor(MinecraftServer server, CallbackInfo ci) {
		this.register(new AllowGatewayCommand());
		this.register(new LogCommand());
		this.register(new DisplayItemCommand());
		this.register(new PingCommand());
		this.register(new PlayerCommand());
		this.register(new SearchForItemCommand());
		this.register(new StackBoxesCommand());
		this.register(new TickCommand());
	}

	@Inject(method = "sendSuccess", at = @At("HEAD"), cancellable = true)
	private void silenceRcon(CommandSource source, Command command, int flags, String message, Object[] args, CallbackInfo ci) {
		if (source.getName().equals("Rcon")) {
			ci.cancel();
		}
	}
}
