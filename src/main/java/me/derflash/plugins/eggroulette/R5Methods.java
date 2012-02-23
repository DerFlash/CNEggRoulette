package me.derflash.plugins.eggroulette;

import org.bukkit.entity.Entity;

public class R5Methods {

	public static boolean checkChicken(Entity entity) {
		// hacky, but will work in >=1.1 R5
    	try {
    		org.bukkit.entity.EntityType type = entity.getType();
        	if (type == org.bukkit.entity.EntityType.CHICKEN) {
        		return true;
        	}
    	} catch(NoClassDefFoundError e) {}
    	
    	return false;
	}

}
