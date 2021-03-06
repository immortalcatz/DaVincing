package two.davincing.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import two.davincing.DaVincing;
import two.davincing.ProxyBase;
import two.davincing.sculpture.Operations;
import two.davincing.sculpture.Sculpture;
import two.davincing.sculpture.SculptureEntity;
import two.davincing.utils.Utils;

public class CopygunItem extends ItemBase {

  public CopygunItem() {
    this.setUnlocalizedName("copygun");
    this.setTextureName("minepainter:copygun");
    this.setMaxStackSize(1);
    this.setMaxDamage(512);
  }

  @Override
  public boolean getShareTag() {
    return true;
  }

  @Override
  public boolean onItemUse(final ItemStack heldItem, final EntityPlayer player, final World world, int x, int y, int z, int face, float xs, float ys, float zs) {
    final Block targetBlock = world.getBlock(x, y, z);
    if (targetBlock != ProxyBase.blockSculpture.getBlock()) {
      int meta = world.getBlockMetadata(x, y, z);
      if (targetBlock != Blocks.air && Operations.sculptable(targetBlock, meta)) {
        int block_sig = Block.getIdFromBlock(targetBlock) << 4;
        block_sig += meta;

        int prev = getCharge(heldItem, block_sig);
        if (prev + 512 > Short.MAX_VALUE) {
          return false;
        }

        setCharge(heldItem, block_sig, prev + 512);
        return world.setBlockToAir(x, y, z);
      }
      return false;
    }

    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntity instanceof SculptureEntity) {
      final SculptureEntity se = (SculptureEntity) tileEntity;
      final Sculpture sculpture = se.sculpture();

      if (sculpture.isEmpty()) {
        return false;
      }
      int[][] sigs = sculpture.getBlockSigs();
      for (int i = 0; i < sigs[0].length; i++) {
        if (sigs[0][i] == 0) {
          break;
        }
        if (getCharge(heldItem, sigs[0][i]) < sigs[1][i]) {
          return false;
        }
      }
      for (int i = 0; i < sigs[0].length; i++) {
        if (sigs[0][i] == 0) {
          break;
        }
        int sig = sigs[0][i];
        int count = sigs[1][i];
        setCharge(heldItem, sig, getCharge(heldItem, sig) - count);
      }

      if (!player.capabilities.isCreativeMode) {
        heldItem.damageItem(1, player);
      }

      return ProxyBase.blockSculpture.getBlock().dropSculptureToPlayer(world, player, x, y, z);
    } else {
      DaVincing.log.warn("[CopygunItem.onItemUse] failed: expected SculptureEntity at %d, %d, %d, but got %s", x, y, z, Utils.getClassName(tileEntity));
      return false;
    }
  }

  public int getCharge(ItemStack is, int block_sig) {
    NBTTagCompound nbt = is.getTagCompound();
    if (nbt == null) {
      return 0;
    }
    String key = "bs:" + block_sig;
    if (!nbt.hasKey(key)) {
      return 0;
    }
    return nbt.getShort(key);
  }

  public void setCharge(ItemStack is, int block_sig, int count) {
    NBTTagCompound nbt = is.getTagCompound();
    if (nbt == null) {
      is.setTagCompound(nbt = new NBTTagCompound());
    }
    String key = "bs:" + block_sig;
    nbt.setShort(key, (short) count);
  }

  @Override
  public void addInformation(ItemStack is, EntityPlayer ep, List list, boolean help) {
    NBTTagCompound nbt = is.getTagCompound();
    if (nbt == null) {
      return;
    }

    for (Object key : nbt.func_150296_c()) {
      String str = (String) key;
      if (str.startsWith("bs:")) {
        short sig = Short.parseShort(str.substring(3));
        Block block = Block.getBlockById(sig >>> 4);
        int meta = sig & 0xf;
        list.add(new ItemStack(block, 1, meta).getDisplayName() + " x " + nbt.getShort(str));
      }
    }
  }
}
