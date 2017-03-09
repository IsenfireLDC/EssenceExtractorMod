package gamedev.eem.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
	
	public static void init() {
		
	}
	
	private static <T extends Item> T register(T item) {
		GameRegistry.register(item);
		
		return item;
	}

}
