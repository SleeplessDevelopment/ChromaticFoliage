package dev.sapphic.chromaticfoliage.client;

import dev.sapphic.chromaticfoliage.ChromaticFoliage;
import dev.sapphic.chromaticfoliage.client.particle.ParticleData;
import dev.sapphic.chromaticfoliage.client.particle.ParticleHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

public final class ChromaticParticles {
  private static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(ChromaticFoliage.ID);

  private ChromaticParticles() {
  }

  public static void init() {
    WRAPPER.registerMessage(ParticleHandler.class, ParticleData.class, 0, Side.CLIENT);
  }

  public static void landing(final IBlockState state, final WorldServer world, final BlockPos pos, final EntityLivingBase entity, final int count, final boolean emissive) {
    final double x = entity.posX;
    final double y = entity.posY;
    final double z = entity.posZ;
    WRAPPER.sendToAllAround(new ParticleData(count, new Vec3d(x, y, z), Vec3d.ZERO, 0.15, state, emissive), new NetworkRegistry.TargetPoint(entity.dimension, x, y, z, 1024.0));
  }

  public static void sprinting(final Random rand, final IBlockState state, final World world, final BlockPos pos, final Entity entity, final boolean emissive) {
    final double x = entity.posX + ((rand.nextFloat() - 0.5) * entity.width);
    final double y = entity.getEntityBoundingBox().minY + 0.1;
    final double z = entity.posZ + ((rand.nextFloat() - 0.5) * entity.width);
    final Vec3d velocity = new Vec3d(-entity.motionX * 4.0, 1.5, -entity.motionZ * 4.0);
    WRAPPER.sendToAllAround(new ParticleData(new Vec3d(x, y, z), velocity, 0.0, state, emissive), new NetworkRegistry.TargetPoint(entity.dimension, x, y, z, 1024.0));
  }
}
