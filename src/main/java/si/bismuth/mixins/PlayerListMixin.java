package si.bismuth.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.MCServer;
import si.bismuth.discord.DCBot;
import si.bismuth.patches.EntityPlayerMPFake;
import si.bismuth.patches.NetHandlerPlayServerFake;
import si.bismuth.utils.IWorldServer;

import java.util.regex.Pattern;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	final private static Pattern urlPattern = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	private EntityPlayerMP mycopy;

	@Inject(method = "playerLoggedIn", at = @At(value = "RETURN"))
	private void onPlayerLoggedIn(EntityPlayerMP player, CallbackInfo ci) {
		MCServer.playerConnected(player);
	}

	@Inject(method = "playerLoggedOut", at = @At(value = "HEAD"))
	private void onPlayerLoggedOut(EntityPlayerMP player, CallbackInfo ci) {
		MCServer.playerDisconnected(player);
	}

	@Inject(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/management/PlayerList;readPlayerDataFromFile(Lnet/minecraft/entity/player/EntityPlayerMP;)Lnet/minecraft/nbt/NBTTagCompound;"))
	private void onInitializeConnectionToPlayer(NetworkManager manager, EntityPlayerMP player, CallbackInfo ci) {
		if (player instanceof EntityPlayerMPFake) {
			((EntityPlayerMPFake) player).resetToSetPosition();
		}
	}

	@Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
	private NetHandlerPlayServer replaceNetHandler(MinecraftServer server, NetworkManager manager, EntityPlayerMP player) {
		return player instanceof EntityPlayerMPFake ? new NetHandlerPlayServerFake(server, manager, player) : new NetHandlerPlayServer(server, manager, player);
	}

	@Redirect(method = "createPlayerForUser", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;connection:Lnet/minecraft/network/NetHandlerPlayServer;"))
	private NetHandlerPlayServer copyVariable(EntityPlayerMP player) {
		this.mycopy = player;
		return player.connection;
	}

	@Redirect(method = "createPlayerForUser", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
	private void handleFakePlayerJoin(NetHandlerPlayServer handler, ITextComponent component) {
		if (this.mycopy instanceof EntityPlayerMPFake) {
			this.mycopy.onKillCommand();
		} else {
			this.mycopy.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
		}
	}

	@Inject(method = "transferEntityToWorld", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
	private void onTransferEntityToWorld(Entity entity, int lastDimension, WorldServer oldWorld, WorldServer newWorld, CallbackInfo ci) {
		if (entity.addedToChunk && ((IWorldServer) oldWorld).isChunkLoadedC(entity.chunkCoordX, entity.chunkCoordZ, true)) {
			if (entity.addedToChunk && ((IWorldServer) oldWorld).isChunkLoadedC(entity.chunkCoordX, entity.chunkCoordZ, true)) {
				oldWorld.getChunk(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);
			}
		}
	}

	@Inject(method = "sendMessage(Lnet/minecraft/util/text/ITextComponent;Z)V", at = @At("HEAD"))
	private void onPlayerSendMessage(ITextComponent component, boolean isSystem, CallbackInfo ci) {
		if (!isSystem) {
			final String text = component.getUnformattedText().replaceFirst("<(\\S*?)>", "<`$1`>");
			MCServer.bot.jda.getTextChannelById(DCBot.ChannelID).sendMessage(text).queue();
		}
	}

	/*
	@ModifyVariable(method = "sendMessage(Lnet/minecraft/util/text/ITextComponent;Z)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private ITextComponent applyTextFormatting(ITextComponent component) {
		return this.applyFormatting(component);
	}

	// mega shitty and fragile code.
	//TODO: Fix plz
	private ITextComponent applyFormatting(ITextComponent component) {
		// Current handle two cases:
		// * DC chat which is TextComponentString{[TextComponentString{prefix},TextComponentTranslation{name,chat}]}
		// * IG chat which is TextComponentTranslation{name,chat}
		// mega hax lol.
		System.out.println(component.getClass());

		TextComponentTranslation startComponent;
		try {
			if (component instanceof TextComponentString) {
				startComponent = (TextComponentTranslation) component.getSiblings().get(1);
			} else {
				startComponent = (TextComponentTranslation) component;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return component;
		}

		// code stolen from Whitefang34's post on Stackoverflow: https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string/5713866#5713866
		// Pattern for recognizing a URL, based off RFC 3986
		final Matcher matcher = urlPattern.matcher(startComponent.getUnformattedComponentText());
		final ITextComponent newComponent = new TextComponentTranslation();
		int lastMatchEnd = 0;
		while (matcher.find()) {
			final int matchStart = matcher.start(1);
			final int matchEnd = matcher.end();

		}

		return newComponent;
	}*/
}
