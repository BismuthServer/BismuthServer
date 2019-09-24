package carpet.bismuth.mixins;

import carpet.bismuth.utils.IWorldServer;
import carpet.bismuth.patches.EntityPlayerMPFake;
import carpet.bismuth.patches.NetHandlerPlayServerFake;
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

@Mixin(PlayerList.class)
abstract class PlayerListMixin {
	private EntityPlayerMP mycopy;

	@Inject(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/management/PlayerList;readPlayerDataFromFile(Lnet/minecraft/entity/player/EntityPlayerMP;)Lnet/minecraft/nbt/NBTTagCompound;"))
	private void onInitializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, CallbackInfo ci) {
		if (playerIn instanceof EntityPlayerMPFake) {
			((EntityPlayerMPFake) playerIn).resetToSetPosition();
		}
	}

	@Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
	private NetHandlerPlayServer replaceNetHandler(MinecraftServer server, NetworkManager netManager, EntityPlayerMP playerIn) {
		boolean isEntityPlayerMP = playerIn instanceof EntityPlayerMPFake;
		if (isEntityPlayerMP) {
			return new NetHandlerPlayServerFake(server, netManager, playerIn);
		} else {
			return new NetHandlerPlayServer(server, netManager, playerIn);
		}
	}

	@Redirect(method = "createPlayerForUser", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;connection:Lnet/minecraft/network/NetHandlerPlayServer;"))
	private NetHandlerPlayServer copyVariable(EntityPlayerMP player) {
		this.mycopy = player;
		return player.connection;
	}


	@Redirect(method = "createPlayerForUser", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
	private void handleFakePlayerJoin(NetHandlerPlayServer netHandlerPlayServer, ITextComponent textComponent) {
		if (this.mycopy instanceof EntityPlayerMPFake) {
			this.mycopy.onKillCommand();
		} else {
			this.mycopy.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
		}
	}

	@Inject(method = "transferEntityToWorld", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/profiler/Profiler;endSection()V"))
	private void onTransferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, CallbackInfo ci) {
		if (entityIn.addedToChunk && ((IWorldServer) oldWorldIn).isChunkLoadedC(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
			if (entityIn.addedToChunk && ((IWorldServer) oldWorldIn).isChunkLoadedC(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
				oldWorldIn.getChunk(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
			}
		}
	}
}
