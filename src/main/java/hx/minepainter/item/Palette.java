package hx.minepainter.item;

import hx.minepainter.ModMinePainter;
import hx.minepainter.painting.PaintingEntity;
import hx.minepainter.painting.PaintingPlacement;
import hx.utils.Utils;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class Palette extends Item{
	
    private IIcon[] colors = new IIcon[6];

	public Palette() {
		super();
		setCreativeTab(ModMinePainter.tabMinePainter);
		this.setMaxStackSize(1);
		this.setTextureName("minepainter:palette");
		this.setUnlocalizedName("palette");
	}
	
        @Override
	public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
	
        @Override
	public int getRenderPasses(int metadata)
	{
		return 7;
	}
	
        @Override
	public IIcon getIcon(ItemStack is, int renderPass)
	{
		if(renderPass == 0)return itemIcon;
		return colors[renderPass - 1];
	}
        
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        super.registerIcons(par1IconRegister);
        for(int i =0;i<6;i++)colors[i]=par1IconRegister.registerIcon(this.getIconString() + i);
    }
	
        @Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
	{
		int[] colors = getColors(par1ItemStack);
		
		if(par2 == 0)return super.getColorFromItemStack(par1ItemStack, par2);
		return colors[par2 - 1];
	}
	
	public static int[] getColors(ItemStack is)
	{
		NBTTagCompound nbt = is.getTagCompound();
		if(nbt == null) is.setTagCompound(nbt = new NBTTagCompound());
		
		NBTTagCompound palette = nbt.getCompoundTag("palette");
		int[] colors = palette.getIntArray("colors");
		if(colors.length == 0)colors =  new int[]{0xffffffff,0xffffffff,0xffffffff,0xffffffff,0xffffffff,0xffffffff};
		
		palette.setIntArray("colors", colors);
		nbt.setTag("palette", palette);
		
		return colors;
	}
	
        @Override
	public boolean onItemUse(ItemStack is, EntityPlayer ep, World w, int x, int y, int z, int par7, float _x, float _y, float _z)
	{
		if(w.getBlock(x, y, z) == ModMinePainter.painting.block)
		{
			int face = w.getBlockMetadata(x, y, z );
			PaintingEntity pe = Utils.getTE(w, x, y, z);
			PaintingPlacement pp = PaintingPlacement.of(face);
			float[] point = pp.block2painting(_x, _y, _z);
			
			int px = (int) (point[0] * 16 + 16) - 16;
			int py = (int) (point[1] * 16 + 16) - 16;
			if(px>15 || px<0 || py>15||py<0)return false;
			
			int[] colors = getColors(is); 
			colors[0] = pe.getImg().getRGB(px,py);
			setColors(is, colors);
			return true;
		}
		
		return false;
	}
	
	
        @Override
	public ItemStack onItemRightClick(ItemStack is, World w, EntityPlayer ep)
    {
		setColors(is, shift(getColors(is)));
        return is;
    } 
	
	public static int[] shift(int[] colors)
	{
		int t = colors[0];
		for(int i = 1; i < colors.length; i ++)
			colors[i-1] = colors[i];
		colors[colors.length-1]=t;
		
		return colors;
	}
	
	public static void setColors(ItemStack is, int[] colors)
	{
		NBTTagCompound nbt = is.getTagCompound();
		if(nbt == null) is.setTagCompound(nbt = new NBTTagCompound());
		
		NBTTagCompound palette = nbt.getCompoundTag("palette");
		palette.setIntArray("colors", colors);
		nbt.setTag("palette", palette);
	}
	
        @Override
	public void addInformation(ItemStack is, EntityPlayer ep, List list, boolean help)
	{
		int color = getColors(is)[0];
		list.add("Alpha : " 	+ ((color >> 24) & 0xff));
		list.add("\u00a7cRed : " 	+ ((color >> 16) & 0xff));
		list.add("\u00a7aGreen : " 	+ ((color >>  8) & 0xff));
		list.add("\u00a79Blue : " 	+ ((color >>  0) & 0xff));
	}
}
