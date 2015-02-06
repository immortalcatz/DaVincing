package two.davincing.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;
import two.davincing.ProxyBase;
import two.davincing.painting.PaintingEntity;
import two.davincing.utils.ExpirablePool;

@SideOnly(Side.CLIENT)
public class CanvasRenderer implements IItemRenderer {

  protected ExpirablePool<ItemStack, PaintingTexture> itemIconCache = new ExpirablePool<ItemStack, PaintingTexture>() {

    @Override
    protected void release(final PaintingTexture paintingIcon) {
      paintingIcon.dispose();
    }

    @Override
    protected PaintingTexture create(final ItemStack key) {
      final BufferedImage image = PaintingEntity.getPaintingFromItem(key);
      final PaintingTexture result = new PaintingTexture();
      result.setRGB(image);
      return result;
    }

  };

  public static boolean overrideUseRenderHelper = false;

  @Override
  public boolean handleRenderType(ItemStack item, ItemRenderType type) {
    return type == ItemRenderType.INVENTORY
            || type == ItemRenderType.EQUIPPED
            || type == ItemRenderType.EQUIPPED_FIRST_PERSON
            || type == ItemRenderType.ENTITY;
  }

  @Override
  public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
    if (overrideUseRenderHelper) {
      return true;
    }
    if (type == ItemRenderType.ENTITY) {
      return helper == ItemRendererHelper.ENTITY_ROTATION
              || helper == ItemRendererHelper.ENTITY_BOBBING;
    }
    return false;
  }

  @Override
  public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
    IIcon icon;
    if (item.hasTagCompound()) {
      final PaintingTexture paintingTexture = itemIconCache.get(item);
      paintingTexture.bind();
      icon = paintingTexture;
    } else {
      icon = ProxyBase.itemCanvas.getIconFromDamage(0);
    }
    if (type == ItemRenderType.INVENTORY) {
      renderInventory(icon);
    } else if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
      renderEquipped(icon);
    } else {
      GL11.glTranslatef(-0.5f, 0, 0);
      renderEquipped(icon);
    }
  }

  private void renderInventory(IIcon icon) {
    Tessellator tes = Tessellator.instance;
    tes.startDrawingQuads();
    tes.addVertexWithUV(1, 1, 0, icon.getMinU(), icon.getMinV());
    tes.addVertexWithUV(1, 15, 0, icon.getMinU(), icon.getMaxV());
    tes.addVertexWithUV(15, 15, 0, icon.getMaxU(), icon.getMaxV());
    tes.addVertexWithUV(15, 1, 0, icon.getMaxU(), icon.getMinV());
    tes.draw();
  }

  private void renderEquipped(IIcon icon) {
    Tessellator var5 = Tessellator.instance;
    float var7 = icon.getMinU();
    float var8 = icon.getMaxU();
    float var9 = icon.getMinV();
    float var10 = icon.getMaxV();
    ItemRenderer.renderItemIn2D(var5, var8, var9, var7, var10, 256, 256, 0.0625F);
  }
}
