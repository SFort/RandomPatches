/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.randompatches.mixin.client;

import java.io.InputStream;

import com.mojang.serialization.Lifecycle;
import com.therandomlabs.randompatches.RandomPatches;
import com.therandomlabs.randompatches.client.RPWindowHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {
	@Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
	private void getWindowTitle(CallbackInfoReturnable<String> info) {
		RPWindowHandler.enable();

		if (RandomPatches.config().client.window.customTitle) {
			info.setReturnValue(RPWindowHandler.getWindowTitle());
		}
	}

	@Redirect(method = "<init>", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/Window;setIcon" +
					"(Ljava/io/InputStream;Ljava/io/InputStream;)V"
	))
	private void setIcon(Window window, InputStream stream16, InputStream stream32) {
		RPWindowHandler.updateWindowIcon(stream16, stream32);
	}

	@Redirect(
			method = "startIntegratedServer(Ljava/lang/String;" +
					"Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;" +
					"Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;" +
					"ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V",
			at = @At(
					value = "INVOKE",
					target = "net/minecraft/world/SaveProperties.getLifecycle()" +
							"Lcom/mojang/serialization/Lifecycle;"
			)
	)
	private Lifecycle getLifecycle(SaveProperties properties) {
		return RandomPatches.config().client.disableExperimentalSettingsWarning ?
				Lifecycle.stable() : properties.getLifecycle();
	}
}
