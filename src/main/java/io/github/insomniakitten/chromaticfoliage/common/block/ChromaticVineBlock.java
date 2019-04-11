package io.github.insomniakitten.chromaticfoliage.common.block;

import io.github.insomniakitten.chromaticfoliage.common.ChromaticFoliage;
import io.github.insomniakitten.chromaticfoliage.common.base.ChromaticColor;
import io.github.insomniakitten.chromaticfoliage.common.block.entity.ChromaticBlockEntity;
import io.github.insomniakitten.chromaticfoliage.common.init.ChromaticBlocks;
import io.github.insomniakitten.chromaticfoliage.common.init.ChromaticItems;
import io.github.insomniakitten.chromaticfoliage.common.init.ChromaticSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;

public class ChromaticVineBlock extends BlockVine implements ChromaticBlock {
  private final boolean isReplaceable;

  public ChromaticVineBlock() {
    isReplaceable = ChromaticFoliage.getGeneralConfig().areVinesReplaceable();
    setHardness(0.2F);
    setSoundType(SoundType.PLANT);
  }

  @Override
  @Deprecated
  public MapColor getMapColor(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
    return state.getActualState(access, pos).getValue(COLOR).getMapColor();
  }

  @Override
  public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
    final ItemStack stack = player.getHeldItem(hand);
    if (!player.canPlayerEdit(pos, facing, stack)) return false;
    if (stack.isEmpty()) return false;
    if (ChromaticFoliage.getGeneralConfig().isInWorldIlluminationEnabled() && Items.GLOWSTONE_DUST == stack.getItem()) {
      if (world.isRemote) return true;
      final IBlockState actualState = state.getActualState(world, pos);
      IBlockState vine = ChromaticBlocks.emissiveVine().getDefaultState();
      for (final Entry<IProperty<?>, Comparable<?>> prop : actualState.getProperties().entrySet()) {
        //noinspection unchecked,RedundantCast
        vine = vine.withProperty((IProperty) prop.getKey(), (Comparable) prop.getValue());
      }
      if (!world.setBlockState(pos, vine, 3)) return false;
      world.playSound(null, pos, ChromaticSounds.blockIlluminated(), SoundCategory.BLOCKS, 1.0F, 0.8F);
      if (!player.capabilities.isCreativeMode) {
        stack.shrink(1);
      }
      return true;
    }
    if (ChromaticFoliage.getGeneralConfig().isChromaRecoloringEnabled()) {
      final Optional<ChromaticColor> optional = ChromaticColor.byDyeColor(stack);
      if (!optional.isPresent()) return false;
      final ChromaticColor color = optional.get();
      if (color == state.getValue(COLOR)) return false;
      if (world.isRemote) return true;
      if (!world.setBlockState(pos, state.withProperty(COLOR, color), 3)) return false;
      world.playSound(null, pos, ChromaticSounds.blockDyed(), SoundCategory.BLOCKS, 1.0F, 0.8F);
      if (!player.capabilities.isCreativeMode) stack.shrink(1);
      return true;
    }
    return false;
  }

  @Override
  protected ItemStack getSilkTouchDrop(final IBlockState state) {
    return new ItemStack(ChromaticItems.chromaticVine(), 1, state.getValue(COLOR).ordinal());
  }

  @Override
  public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
    @Nullable final TileEntity te = world.getTileEntity(pos);
    if (te instanceof ChromaticBlockEntity) {
      ((ChromaticBlockEntity) te).setColor(ChromaticColor.valueOf(stack.getMetadata() & 15));
    }
  }

  @Override
  public void getSubBlocks(final CreativeTabs group, final NonNullList<ItemStack> items) {
    for (final ChromaticColor color : ChromaticColor.colors()) {
      items.add(new ItemStack(this, 1, color.ordinal()));
    }
  }

  @Override
  public String toString() {
    return "ChromaticVineBlock";
  }

  @Override
  public boolean hasTileEntity(final IBlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(final World world, final IBlockState state) {
    return new ChromaticBlockEntity(state.getValue(COLOR));
  }

  @Override
  public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
    return getSilkTouchDrop(state.getActualState(world, pos));
  }

  @Override
  public boolean recolorBlock(final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color) {
    final IBlockState state = world.getBlockState(pos);
    checkState(state.getBlock() == this, "Unexpected block %s at %s", state, pos);
    return world.setBlockState(pos, state.withProperty(COLOR, ChromaticColor.byDyeColor(color)));
  }

  @Override
  public IBlockState getActualState(final IBlockState state, final IBlockAccess access, final BlockPos pos) {
    final IBlockState actualState = super.getActualState(state, access, pos);
    @Nullable final TileEntity te = access.getTileEntity(pos);
    if (te instanceof ChromaticBlockEntity) {
      return actualState.withProperty(COLOR, ((ChromaticBlockEntity) te).getColor());
    }
    return actualState;
  }

  @Override
  public boolean isReplaceable(final IBlockAccess access, final BlockPos pos) {
    return isReplaceable;
  }

  @Override
  public boolean canAttachTo(final World world, final BlockPos pos, final EnumFacing side) {
    final Block block = world.getBlockState(pos.up()).getBlock();
    return isAcceptableNeighbor(world, pos.offset(side.getOpposite()), side)
      && (block == Blocks.AIR || block instanceof BlockVine
      || isAcceptableNeighbor(world, pos.up(), EnumFacing.UP));
  }

  @Override
  public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
    if (!world.isRemote && !recheckGrownSides(world, pos, state)) {
      dropBlockAsItem(world, pos, state, 0);
      world.setBlockToAir(pos);
    }
  }

  @Override
  public void updateTick(final World world, final BlockPos pos, IBlockState state, final Random rand) {
    if (world.isRemote || world.rand.nextInt(4) != 0 || !world.isAreaLoaded(pos, 4)) {
      return;
    }
    int j = 5;
    boolean flag = false;
    areaCheck:
    for (int x = -4; x <= 4; ++x) {
      for (int y = -4; y <= 4; ++y) {
        for (int z = -1; z <= 1; ++z) {
          final Block block = world.getBlockState(pos.add(x, z, y)).getBlock();
          if (block instanceof BlockVine) {
            --j;
            if (j <= 0) {
              flag = true;
              break areaCheck;
            }
          }
        }
      }
    }
    final EnumFacing randSide = EnumFacing.random(rand);
    final BlockPos posUp = pos.up();
    if (randSide == EnumFacing.UP && pos.getY() < 255 && world.isAirBlock(posUp)) {
      state = state.getActualState(world, pos);
      IBlockState stateAt = state;
      for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
        if (rand.nextBoolean() && canAttachTo(world, posUp, side.getOpposite())) {
          stateAt = stateAt.withProperty(getPropertyFor(side), true);
        } else {
          stateAt = stateAt.withProperty(getPropertyFor(side), false);
        }
      }
      if (stateAt.getValue(BlockVine.NORTH) || stateAt.getValue(BlockVine.EAST)
        || stateAt.getValue(BlockVine.SOUTH) || stateAt.getValue(BlockVine.WEST)) {
        world.setBlockState(posUp, stateAt, 2);
      }
    } else if (randSide.getAxis().isHorizontal() && !state.getValue(getPropertyFor(randSide))) {
      if (flag) {
        return;
      }
      final BlockPos blockpos4 = pos.offset(randSide);
      final IBlockState iblockstate3 = world.getBlockState(blockpos4);
      final Block block1 = iblockstate3.getBlock();
      if (block1.isAir(iblockstate3, world, blockpos4)) {
        final EnumFacing rotY = randSide.rotateY();
        final EnumFacing rotYCCW = randSide.rotateYCCW();
        final boolean valRotY = state.getValue(getPropertyFor(rotY));
        final boolean valRotYCCW = state.getValue(getPropertyFor(rotYCCW));
        final BlockPos posRotY = blockpos4.offset(rotY);
        final BlockPos posRotYCCW = blockpos4.offset(rotYCCW);
        final ChromaticColor color = state.getActualState(world, pos).getValue(COLOR);
        final IBlockState retState = getDefaultState().withProperty(COLOR, color);
        if (valRotY && canAttachTo(world, posRotY.offset(rotY), rotY)) {
          world.setBlockState(blockpos4, retState.withProperty(getPropertyFor(rotY), true), 2);
        } else if (valRotYCCW && canAttachTo(world, posRotYCCW.offset(rotYCCW), rotYCCW)) {
          world.setBlockState(blockpos4, retState.withProperty(getPropertyFor(rotYCCW), true), 2);
        } else if (valRotY && world.isAirBlock(posRotY) && canAttachTo(world, posRotY, randSide)) {
          world.setBlockState(posRotY, retState.withProperty(getPropertyFor(randSide.getOpposite()), true), 2);
        } else if (valRotYCCW && world.isAirBlock(posRotYCCW) && canAttachTo(world, posRotYCCW, randSide)) {
          world.setBlockState(posRotYCCW, retState.withProperty(getPropertyFor(randSide.getOpposite()), true), 2);
        }
      } else if (iblockstate3.getBlockFaceShape(world, blockpos4, randSide) == BlockFaceShape.SOLID) {
        world.setBlockState(pos, state.withProperty(getPropertyFor(randSide), true), 2);
      }
    } else if (pos.getY() > 1) {
      final BlockPos posDown = pos.down();
      final IBlockState below = world.getBlockState(posDown);
      final Block block = below.getBlock();
      if (below.getMaterial() == Material.AIR) {
        IBlockState originalState = state;
        for (final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
          if (rand.nextBoolean()) {
            originalState = originalState.withProperty(getPropertyFor(enumfacing), false);
          }
        }
        if (originalState.getValue(BlockVine.NORTH) || originalState.getValue(BlockVine.EAST)
          || originalState.getValue(BlockVine.SOUTH) || originalState.getValue(BlockVine.WEST)) {
          world.setBlockState(posDown, originalState, 2);
        }
      } else if (block instanceof BlockVine) {
        IBlockState originalState = below;
        for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
          final PropertyBool prop = getPropertyFor(side);
          if (rand.nextBoolean() && state.getValue(prop)) {
            originalState = originalState.withProperty(prop, true);
          }
        }
        if (originalState.getValue(BlockVine.NORTH) || originalState.getValue(BlockVine.EAST)
          || originalState.getValue(BlockVine.SOUTH) || originalState.getValue(BlockVine.WEST)) {
          world.setBlockState(posDown, originalState, 2);
        }
      }
    }
  }

  @Override
  public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
    return super.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, meta, placer)
      .withProperty(COLOR, ChromaticColor.valueOf(meta & 15));
  }

  @Override
  public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, final TileEntity tile, final ItemStack stack) {
    if (!world.isRemote && stack.getItem() == Items.SHEARS) {
      player.addStat(Objects.requireNonNull(StatList.getBlockStats(this)));
      Block.spawnAsEntity(world, pos, new ItemStack(ChromaticItems.chromaticVine(), 1, 0));
    } else {
      super.harvestBlock(world, player, pos, state, tile, stack);
    }
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, COLOR, UP, NORTH, SOUTH, WEST, EAST);
  }

  private boolean isAcceptableNeighbor(final World world, final BlockPos pos, final EnumFacing side) {
    final IBlockState state = world.getBlockState(pos);
    final BlockFaceShape shape = state.getBlockFaceShape(world, pos, side);
    return shape == BlockFaceShape.SOLID && !BlockVine.isExceptBlockForAttaching(state.getBlock());
  }

  private boolean recheckGrownSides(final World world, final BlockPos pos, final IBlockState state) {
    IBlockState actualState = state.getActualState(world, pos);
    final IBlockState originalState = actualState;
    for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
      final PropertyBool property = BlockVine.getPropertyFor(side);
      if (actualState.getValue(property) && !canAttachTo(world, pos, side.getOpposite())) {
        final IBlockState above = world.getBlockState(pos.up());
        if (!(above.getBlock() instanceof BlockVine) || !above.getValue(property)) {
          actualState = actualState.withProperty(property, false);
        }
      }
    }
    if (BlockVine.getNumGrownFaces(actualState) != 0) {
      if (originalState != actualState) {
        world.setBlockState(pos, actualState, 2);
      }
      return true;
    }
    return false;
  }
}
