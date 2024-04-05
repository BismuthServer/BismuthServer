package si.bismuth.stats.mixins.StatsScreen;

import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import si.bismuth.stats.IPlayerStats;
import si.bismuth.stats.IStat;

@Mixin(StatsScreen.AbstractStatsListWidget.class)
class AbstractStatsListWidgetMixin {
    @Shadow @Final
    StatsScreen statsScreen;

    @Redirect(method = "m_0499830", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/Stat;format(I)Ljava/lang/String;"))
    public String m_0499830(Stat instance, int value) {
        return ((IStat) instance).bismuthServer$longFormat(((IPlayerStats) statsScreen.stats).bismuthServer$getLongStat(instance));
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 115))
    private int injected1(int value) {
        return 140;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 165))
    private int injected2(int value) {
        return 240;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 215))
    private int injected3(int value) {
        return 340;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 265))
    private int injected4(int value) {
        return 440;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 315))
    private int injected5(int value) {
        return 540;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 79))
    private int injected6(int value) {
        return 104;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 129))
    private int injected7(int value) {
        return 204;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 179))
    private int injected8(int value) {
        return 304;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 229))
    private int injected9(int value) {
        return 404;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 279))
    private int injected10(int value) {
        return 504;
    }

    @ModifyConstant(method = "getRowWidth", constant = @Constant(intValue = 375))
    private int rowWidth(int value) {
        return 600;
    }

    @ModifyConstant(method = "*", constant = @Constant(intValue = 140))
    private int fixScrollbarPosition(int value) {
        return 250;
    }
}
