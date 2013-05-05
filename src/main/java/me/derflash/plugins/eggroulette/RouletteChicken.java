package me.derflash.plugins.eggroulette;

import java.lang.reflect.Field;
import net.minecraft.server.v1_5_R3.EntityAgeable;
import net.minecraft.server.v1_5_R3.EntityChicken;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.ItemSeeds;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_5_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_5_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_5_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.Location;

public class RouletteChicken extends EntityChicken {

    public boolean isRetarded = false;

    public RouletteChicken(final Location loc, final World world) {
        super(world);

        // Prepare a cleared goal-list
        // overwriting a private final field? no problem!
        final PathfinderGoalSelector newGoals = new PathfinderGoalSelector(world != null && world.methodProfiler != null ? world.methodProfiler : null);
        newGoals.a(0, new PathfinderGoalFloat(this));
        newGoals.a(1, new PathfinderGoalRandomStroll(this, 0.25F));
        newGoals.a(2, new PathfinderGoalRandomLookaround(this));
        try {
            final Field goals = EntityLiving.class.getDeclaredField("goalSelector");
            goals.setAccessible(true);
            goals.set(this, newGoals);
        } catch (Exception e) {
            System.out.println(this + " error overriding entity goals!");
            e.printStackTrace();
        }

        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        this.yaw = loc.getYaw() + 180;

        while (this.yaw > 360) {
            this.yaw -= 360;
        }

        while (this.yaw < 0) {
            this.yaw += 360;
        }

        if (this.yaw < 45
                || this.yaw > 315) {
            this.yaw = 0F;
        } else if (this.yaw < 135) {
            this.yaw = 90F;
        } else if (this.yaw < 225) {
            this.yaw = 180F;
        } else {
            this.yaw = 270F;
        }

        isRetarded = true;
    }

    public RouletteChicken(final World world) {
        super(world);
    }

    @Override
    public EntityChicken b(EntityAgeable entityageable) {
        if (isRetarded) {
            return new RouletteChicken(this.world);
        } else {
            return new EntityChicken(this.world);
        }
    }

    @Override
    public boolean c(ItemStack itemstack) {
        if (isRetarded) {
            return false; // You can't breed retarded chicken
        } else {
            return itemstack != null && itemstack.getItem() instanceof ItemSeeds;
        }
    }
}
