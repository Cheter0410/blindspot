package com.cheter0410.blindspot;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

public class Blindspot implements ModInitializer {
	public static final String MOD_ID = "blindspot";

	@Override
	public void onInitialize() {

	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
