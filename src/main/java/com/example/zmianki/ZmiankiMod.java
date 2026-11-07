package com.example.zmianki;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ZmiankiMod implements ModInitializer {

    // Szansa na zmiankę (0.25 = 25%)
    private static final float CHANCE = 0.25f;
    // Bonus obrażeń: od MIN do MAX punktów (2 punkty = 1 serce)
    private static final float MIN_BONUS = 2.0f;
    private static final float MAX_BONUS = 6.0f;

    private static boolean applying = false;

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (applying || blocked || damageTaken <= 0) return;
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;
            if (!player.getMainHandStack().isIn(ItemTags.SWORDS)) return;
            if (!(entity.getWorld() instanceof ServerWorld world)) return;
            if (world.random.nextFloat() >= CHANCE) return;

            float bonus = MIN_BONUS + world.random.nextFloat() * (MAX_BONUS - MIN_BONUS);
            applying = true;
            try {
                entity.timeUntilRegen = 0;
                entity.damage(world, source, bonus);
            } finally {
                applying = false;
            }
            player.sendMessage(Text.literal("Zmianka! +" + Math.round(bonus / 2f * 10f) / 10f + " serca")
                    .formatted(Formatting.GOLD), true);
        });
    }
}
