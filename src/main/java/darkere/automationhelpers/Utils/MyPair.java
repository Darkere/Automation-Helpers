package darkere.automationhelpers.Utils;

import net.minecraft.item.ItemStack;

public class MyPair {
    private ItemStack s;
    private int x;

    public MyPair(int x, ItemStack s){
        this.x = x;
        this.s = s;

    }
    public int getX(){
        return x;
    }
    public ItemStack getStack(){
        return s;
    }
}
