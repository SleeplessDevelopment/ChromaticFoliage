package dev.sapphic.chromaticfoliage.block;

import dev.sapphic.chromaticfoliage.ChromaticColor;
import dev.sapphic.chromaticfoliage.ChromaticFoliage;
import dev.sapphic.chromaticfoliage.init.ChromaticBlocks;
import dev.sapphic.chromaticfoliage.init.ChromaticSounds;
import dev.sapphic.chromaticfoliage.ChromaticConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.Random;

public class ChromaticGrassBlock extends BlockGrass {
  private final boolean spreadsToDirt = ChromaticConfig.General.grassSpreadsToDirt;
  private final boolean spreadsToGrass = ChromaticConfig.General.grassSpreadsToGrass;

  public ChromaticGrassBlock() {
    this.setHardness(0.6F);
    this.setSoundType(SoundType.PLANT);
    this.setTickRandomly(this.spreadsToDirt || this.spreadsToGrass);
  }

  @Override
  @Deprecated
  public MapColor getMapColor(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
    return state.getValue(ChromaticFoliage.COLOR).getMapColor();
  }

  @Override
  @Deprecated
  public IBlockState getStateFromMeta(final int meta) {
    return this.getDefaultState().withProperty(ChromaticFoliage.COLOR, ChromaticColor.of(meta));
  }

  @Override
  public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
    final ItemStack stack = player.getHeldItem(hand);
    if (!player.canPlayerEdit(pos, facing, stack) || stack.isEmpty()) {
      return false;
    }
    if (ChromaticConfig.General.inWorldIllumination && (Items.GLOWSTONE_DUST == stack.getItem())) {
      if (world.isRemote) {
        return true;
      }
      final IBlockState grass = ChromaticBlocks.EMISSIVE_GRASS.getDefaultState();
      if (!world.setBlockState(pos, grass.withProperty(ChromaticFoliage.COLOR, state.getValue(ChromaticFoliage.COLOR)), 3)) {
        return false;
      }
      world.playSound(null, pos, ChromaticSounds.BLOCK_ILLUMINATED, SoundCategory.BLOCKS, 1.0F, 0.8F);
      if (!player.capabilities.isCreativeMode) {
        stack.shrink(1);
      }
      return true;
    }
    if (ChromaticConfig.General.recolorRecipes) {
      final Block block = state.getBlock();
      if ((block instanceof IGrowable) && ((IGrowable) block).canUseBonemeal(world, world.rand, pos, state)) {
        if ((stack.getItem() == Items.DYE) && (stack.getMetadata() == EnumDyeColor.WHITE.getDyeDamage())) {
          return false;
        }
      }
      final Optional<ChromaticColor> optional = ChromaticColor.of(stack);
      if (!optional.isPresent()) {
        return false;
      }
      final ChromaticColor color = optional.get();
      if (color == state.getValue(ChromaticFoliage.COLOR)) {
        return false;
      }
      if (world.isRemote) {
        player.swingArm(hand);
        return true;
      }
      if (!world.setBlockState(pos, state.withProperty(ChromaticFoliage.COLOR, color), 3)) {
        return false;
      }
      world.playSound(null, pos, ChromaticSounds.BLOCK_DYED, SoundCategory.BLOCKS, 1.0F, 0.8F);
      if (!player.capabilities.isCreativeMode) {
        stack.shrink(1);
      }
      return true;
    }
    return false;
  }

  @Override
  protected ItemStack getSilkTouchDrop(final IBlockState state) {
    return new ItemStack(this, 1, state.getValue(ChromaticFoliage.COLOR).ordinal());
  }

  @Override
  public void getSubBlocks(final CreativeTabs group, final NonNullList<ItemStack> items) {
    for (final ChromaticColor color : ChromaticColor.COLORS) {
      items.add(new ItemStack(this, 1, color.ordinal()));
    }
  }

  @Override
  public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
    return this.getSilkTouchDrop(state);
  }

  @Override
  public boolean canSustainPlant(final IBlockState state, final IBlockAccess access, final BlockPos pos, final EnumFacing side, final IPlantable plant) {
    return Blocks.GRASS.canSustainPlant(Blocks.GRASS.getDefaultState(), access, pos, side, plant);
  }

  @Override
  public boolean recolorBlock(final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color) {
    return world.setBlockState(pos, world.getBlockState(pos).withProperty(ChromaticFoliage.COLOR, ChromaticColor.of(color)));
  }

  @Override
  public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer, final EnumHand hand) {
    return this.getDefaultState().withProperty(ChromaticFoliage.COLOR, ChromaticColor.of(meta));
  }

  @Override
  public IBlockState getActualState(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
    return state.withProperty(SNOWY, !ChromaticConfig.Client.BLOCKS.snowLayers && this.isSnowBlock(access, pos.up()));
  }

  @Override
  public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
    if ((!this.spreadsToDirt && !this.spreadsToGrass) || world.isRemote || !world.isAreaLoaded(pos, 3)) {
      return;
    }
    final BlockPos above = pos.up();
    final int lightOpacity = world.getBlockState(above).getLightOpacity(world, above);
    if ((lightOpacity <= 2) || (world.getLightFromNeighbors(above) >= 4)) {
      if (world.getLightFromNeighbors(above) < 9) {
        return;
      }
      for (int i = 0; i < 4; ++i) {
        final int x = rand.nextInt(3) - 1;
        final int y = rand.nextInt(5) - 3;
        final int z = rand.nextInt(3) - 1;
        final BlockPos offset = pos.add(x, y, z);
        if (world.isOutsideBuildHeight(offset) || !world.isBlockLoaded(offset)) {
          return;
        }
        if (!this.canSpreadInto(world, offset) || (world.getLightFromNeighbors(offset.up()) < 4) || (lightOpacity > 2)) {
          continue;
        }
        world.setBlockState(offset, state, 3);
      }
    } else {
      world.setBlockState(pos, Blocks.DIRT.getDefaultState());
    }
  }

  @Override // todo: clean this up one day
  public void grow(final World world, final Random rand, final BlockPos pos, final IBlockState state) {
    final BlockPos above = pos.up();
    for (int radius = 0; radius < 128; ++radius) {
      BlockPos targetPos = above;
      int step = 0;
      while (true) {
        if (step >= (radius / 16)) {
          if (world.isAirBlock(targetPos)) {
            if (rand.nextInt(8) == 0) {
              world.getBiome(targetPos).plantFlower(world, rand, targetPos);
            } else {
              final IBlockState tallGrass = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS);
              if (Blocks.TALLGRASS.canBlockStay(world, targetPos, tallGrass)) {
                world.setBlockState(targetPos, tallGrass, 3);
              }
            }
          }
          break;
        }
        targetPos = targetPos.add(rand.nextInt(3) - 1, ((rand.nextInt(3) - 1) * rand.nextInt(3)) / 2, rand.nextInt(3) - 1);
        if (!(world.getBlockState(targetPos.down()).getBlock() instanceof BlockGrass) || world.getBlockState(targetPos).isNormalCube()) {
          break;
        }
        ++step;
      }
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT_MIPPED;
  }

  @Override
  public int getMetaFromState(final IBlockState state) {
    return state.getValue(ChromaticFoliage.COLOR).ordinal();
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, ChromaticFoliage.COLOR, SNOWY);
  }

  protected final boolean canSpreadInto(final IBlockAccess access, final BlockPos pos) {
    final IBlockState state = access.getBlockState(pos);
    if (ChromaticConfig.General.grassSpreadsToDirt && (state.getBlock() == Blocks.DIRT)) {
      if (state.getValue(BlockDirt.VARIANT) == DirtType.DIRT) {
        return true;
      }
    }
    return ChromaticConfig.General.grassSpreadsToGrass && (Blocks.GRASS == state.getBlock());
  }

  protected final boolean isSnowBlock(final IBlockAccess access, final BlockPos pos) {
    final Block block = access.getBlockState(pos).getBlock();
    return (Blocks.SNOW == block) || (Blocks.SNOW_LAYER == block);
  }
}
