var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var CONSUME_INGREDIENT = ASMAPI.mapMethod("func_194325_a");

function log(message) {
	print("[RandomPatches ServerRecipePlacer Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}

	log("Patching method: " + name + " (" + method.name + ")");
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"RandomPatches ServerRecipePlacer Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.item.crafting.ServerRecipePlacer"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;

				for (var i in methods) {
					if (patch(methods[i], CONSUME_INGREDIENT, patchConsumeIngredient)) {
						break;
					}
				}

				return classNode;
			}
		}
	};
}

function patchConsumeIngredient(instructions) {
	var findSlotMatchingUnusedItem;

	for (var i = 0; i < instructions.size(); i++) {
		var instruction = instructions.get(i);

		if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			findSlotMatchingUnusedItem = instruction;
			break;
		}
	}

	//Call ServerRecipeBookHelper#findSlotMatchingUnusedItem
	findSlotMatchingUnusedItem.setOpcode(Opcodes.INVOKESTATIC);
	findSlotMatchingUnusedItem.owner =
		"com/therandomlabs/randompatches/hook/ServerRecipePlacerHook";
	findSlotMatchingUnusedItem.name = "findSlotMatchingUnusedItem";
	findSlotMatchingUnusedItem.desc =
		"(Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/item/ItemStack;)I";
}