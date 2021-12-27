package com.runescape.cache.def;

import com.runescape.Client;
import com.runescape.cache.FileArchive;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.config.VariableBits;
import com.runescape.collection.ReferenceCache;
import com.runescape.entity.model.Model;
import com.runescape.io.Buffer;

/**
 * Refactored reference:
 * http://www.rune-server.org/runescape-development/rs2-client/downloads/575183-almost-fully-refactored-317-client.html
 */
public final class NpcDefinition {
	public static int anInt56;
	public static Buffer dataBuf;
	public static int[] offsets;
	public static NpcDefinition[] cache;
	public static Client clientInstance;
	public static ReferenceCache modelCache = new ReferenceCache(30);
	public int turn90CCWAnimIndex;
	public int varBitID;
	public int turn180AnimIndex;
	public int settingId;
	public int combatLevel;
	public String name;
	public String[] actions;
	public int walkAnim;
	public int size;
	public int[] recolourTarget;
	public int[] additionalModels;
	public int headIcon;
	public int[] recolourOriginal;
	public int standAnim;
	public long interfaceType;
	public int degreesToTurn;
	public int turn90CWAnimIndex;
	public boolean clickable;
	public int lightModifier;
	public int scaleY;
	public boolean drawMinimapDot;
	public int[] childrenIDs;
	public byte[] description;
	public int scaleXZ;
	public int shadowModifier;
	public boolean priorityRender;
	public int[] modelId;
	public int id;

	public NpcDefinition() {
		turn90CCWAnimIndex = -1;
		varBitID = -1;
		turn180AnimIndex = -1;
		settingId = -1;
		combatLevel = -1;
		walkAnim = -1;
		size = 1;
		headIcon = -1;
		standAnim = -1;
		interfaceType = -1L;
		degreesToTurn = 32;
		turn90CWAnimIndex = -1;
		clickable = true;
		scaleY = 128;
		drawMinimapDot = true;
		scaleXZ = 128;
		priorityRender = false;
	}

	/**
	 * Lookup an NpcDefinition by its id
	 *
	 * @param id
	 */
	public static NpcDefinition lookup(int id) {
		for (int index = 0; index < 20; index++)
			if (cache[index].interfaceType == (long) id)
				return cache[index];

		anInt56 = (anInt56 + 1) % 20;
		NpcDefinition definition = cache[anInt56] = new NpcDefinition();
		dataBuf.currentPosition = offsets[id];
		definition.interfaceType = id;
		definition.id = id;
		definition.readValues(dataBuf);

		switch (id) {
		// Pets
		case 497: // Callisto pet
			definition.scaleXZ = 45;
			definition.size = 2;
			break;
		case 6609: // Callisto
			definition.size = 4;
			break;
		case 995:
			definition.recolourOriginal = new int[2];
			definition.recolourTarget = new int[2];
			definition.recolourOriginal[0] = 528;
			definition.recolourTarget[0] = 926;
			break;
		case 7456:
			definition.actions = new String[] { "Repairs", null, null, null, null, null, null };
			break;
		case 1274:
			definition.combatLevel = 35;
			break;
		case 2660:
			definition.combatLevel = 0;
			definition.actions = new String[] { "Trade", null, null, null, null, null, null };
			definition.name = "Pker";
			break;
		case 6477:
			definition.combatLevel = 210;
			break;
		case 6471:
			definition.combatLevel = 131;
			break;
		case 5816:
			definition.combatLevel = 38;
			break;
		case 100:
			definition.drawMinimapDot = true;
			break;
		case 1306:
			definition.actions = new String[] { "Make-over", null, null, null, null, null, null };
			break;
		case 3309:
			definition.name = "Mage";
			definition.actions = new String[] { "Trade", null, "Equipment", "Runes", null, null, null };
			break;
		case 1158:
			definition.name = "@or1@Maxed bot";
			definition.combatLevel = 126;
			definition.actions = new String[] { null, "Attack", null, null, null, null, null };
			definition.modelId[5] = 268; // platelegs rune
			definition.modelId[0] = 18954; // Str cape
			definition.modelId[1] = 21873; // Head - neitznot
			definition.modelId[8] = 15413; // Shield rune defender
			definition.modelId[7] = 5409; // weapon whip
			definition.modelId[4] = 13307; // Gloves barrows
			definition.modelId[6] = 3704; // boots climbing
			definition.modelId[9] = 290; // amulet glory
			break;
		case 1200:
			definition.copy(lookup(1158));
			definition.modelId[7] = 539; // weapon dds
			break;
		case 4096:
			definition.name = "@or1@Archer bot";
			definition.combatLevel = 90;
			definition.actions = new String[] { null, "Attack", null, null, null, null, null };
			definition.modelId[0] = 20423; // cape avas
			definition.modelId[1] = 21873; // Head - neitznot
			definition.modelId[7] = 31237; // weapon crossbow
			definition.modelId[4] = 13307; // Gloves barrows
			definition.modelId[6] = 3704; // boots climbing
			definition.modelId[5] = 20139; // platelegs zammy hides
			definition.modelId[2] = 20157; // platebody zammy hides
			definition.standAnim = 7220;
			definition.walkAnim = 7223;
			definition.turn180AnimIndex = 7220;
			definition.turn90CCWAnimIndex = 7220;
			definition.turn90CWAnimIndex = 7220;
			break;
		case 1576:
			definition.actions = new String[] { "Trade", null, "Equipment", "Ammunition", null, null, null };
			break;
		case 3343:
			definition.actions = new String[] { "Trade", null, "Heal", null, null, null, null };
			break;
		case 506:
		case 526:
			definition.actions = new String[] { "Trade", null, null, null, null, null, null };
			break;
		case 315:
			definition.actions = new String[] { "Talk-to", null, "Trade", "Sell Emblems", "Request Skull", null, null };
			break;

		}
		return definition;
	}

	public static int TOTAL_NPCS;

	public static void init(FileArchive archive) {
        dataBuf = new Buffer(archive.readFile("npc.dat"));
        Buffer idxBuf = new Buffer(archive.readFile("npc.idx"));

		int size = idxBuf.readUShort();
		TOTAL_NPCS = size;

		offsets = new int[size];

		int offset = 2;

		for (int count = 0; count < size; count++) {
			offsets[count] = offset;
			offset += idxBuf.readUShort();
		}

		cache = new NpcDefinition[20];

		for (int count = 0; count < 20; count++) {
			cache[count] = new NpcDefinition();
		}

		System.out.println("Loaded: " + size + " mobs");
	}

	public static void clear() {
		modelCache = null;
		offsets = null;
		cache = null;
		dataBuf = null;
	}

	private void copy(NpcDefinition copy) {
		size = copy.size;
		degreesToTurn = copy.degreesToTurn;
		walkAnim = copy.walkAnim;
		turn180AnimIndex = copy.turn180AnimIndex;
		turn90CWAnimIndex = copy.turn90CWAnimIndex;
		turn90CCWAnimIndex = copy.turn90CCWAnimIndex;
		varBitID = copy.varBitID;
		settingId = copy.settingId;
		combatLevel = copy.combatLevel;
		name = copy.name;
		description = copy.description;
		headIcon = copy.headIcon;
		clickable = copy.clickable;
		lightModifier = copy.lightModifier;
		scaleY = copy.scaleY;
		scaleXZ = copy.scaleXZ;
		drawMinimapDot = copy.drawMinimapDot;
		shadowModifier = copy.shadowModifier;
		actions = new String[copy.actions.length];
		for (int i = 0; i < actions.length; i++) {
			actions[i] = copy.actions[i];
		}
		modelId = new int[copy.modelId.length];
		for (int i = 0; i < modelId.length; i++) {
			modelId[i] = copy.modelId[i];
		}
		priorityRender = copy.priorityRender;
	}

	public Model model() {
		if (childrenIDs != null) {
			NpcDefinition entityDef = morph();
			if (entityDef == null)
				return null;
			else
				return entityDef.model();
		}
		if (additionalModels == null)
			return null;
		boolean flag1 = false;
		for (int index = 0; index < additionalModels.length; index++)
			if (!Model.isCached(additionalModels[index]))
				flag1 = true;

		if (flag1)
			return null;
		Model[] models = new Model[additionalModels.length];
		for (int index = 0; index < additionalModels.length; index++)
			models[index] = Model.getModel(additionalModels[index]);

		Model model;
		if (models.length == 1)
			model = models[0];
		else
			model = new Model(models.length, models);
		if (recolourOriginal != null) {
			for (int index = 0; index < recolourOriginal.length; index++)
				model.recolor(recolourOriginal[index], recolourTarget[index]);

		}
		return model;
	}

	public NpcDefinition morph() {
		int child = -1;
		if (varBitID != -1) {
			VariableBits varBit = VariableBits.varbits[varBitID];
			int variable = varBit.getSetting();
			int low = varBit.getLow();
			int high = varBit.getHigh();
			int mask = Client.BIT_MASKS[high - low];
			child = clientInstance.settings[variable] >> low & mask;
		} else if (settingId != -1)
			child = clientInstance.settings[settingId];
		if (child < 0 || child >= childrenIDs.length || childrenIDs[child] == -1)
			return null;
		else
			return lookup(childrenIDs[child]);
	}

	public Model method164(int j, int frame, int[] ai) {
		if (childrenIDs != null) {
			NpcDefinition entityDef = morph();
			if (entityDef == null)
				return null;
			else
				return entityDef.method164(j, frame, ai);
		}
		Model model = (Model) modelCache.get(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int i1 = 0; i1 < modelId.length; i1++)
				if (!Model.isCached(modelId[i1]))
					flag = true;

			if (flag)
				return null;
			Model[] models = new Model[modelId.length];
			for (int j1 = 0; j1 < modelId.length; j1++)
				models[j1] = Model.getModel(modelId[j1]);

			if (models.length == 1)
				model = models[0];
			else
				model = new Model(models.length, models);
			if (recolourOriginal != null) {
				for (int k1 = 0; k1 < recolourOriginal.length; k1++)
					model.recolor(recolourOriginal[k1], recolourTarget[k1]);

			}
			model.skin();
			model.scale(132, 132, 132);
			model.light(84 + lightModifier, 1000 + shadowModifier, -90, -580, -90, true);
			modelCache.put(model, interfaceType);
		}
		Model empty = Model.EMPTY_MODEL;
		empty.method464(model, Frame.noAnimationInProgress(frame) & Frame.noAnimationInProgress(j));
		if (frame != -1 && j != -1)
			empty.applyAnimationFrames(ai, j, frame);
		else if (frame != -1)
			empty.applyTransform(frame);
		if (scaleXZ != 128 || scaleY != 128)
			empty.scale(scaleXZ, scaleXZ, scaleY);
		empty.calculateDistances();
		empty.faceGroups = null;
		empty.vertexGroups = null;
		if (size == 1)
			empty.fits_on_single_square = true;
		return empty;
	}

	public Model getAnimatedModel(int primaryFrame, int secondaryFrame, int[] interleaveOrder) {
		if (childrenIDs != null) {
			NpcDefinition definition = morph();
			if (definition == null)
				return null;
			else
				return definition.getAnimatedModel(primaryFrame, secondaryFrame, interleaveOrder);
		}
		Model model = (Model) modelCache.get(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int index = 0; index < modelId.length; index++)
				if (!Model.isCached(modelId[index]))
					flag = true;
			if (flag) {
				return null;
			}
			Model[] models = new Model[modelId.length];
			for (int index = 0; index < modelId.length; index++)
				models[index] = Model.getModel(modelId[index]);

			if (models.length == 1)
				model = models[0];
			else
				model = new Model(models.length, models);
			if (recolourOriginal != null) {
				for (int index = 0; index < recolourOriginal.length; index++)
					model.recolor(recolourOriginal[index], recolourTarget[index]);

			}
			model.skin();
			model.light(64 + lightModifier, 850 + shadowModifier, -30, -50, -30, true);
			modelCache.put(model, interfaceType);
		}
		Model model_1 = Model.EMPTY_MODEL;
		model_1.method464(model,
				Frame.noAnimationInProgress(secondaryFrame) & Frame.noAnimationInProgress(primaryFrame));
		if (secondaryFrame != -1 && primaryFrame != -1)
			model_1.applyAnimationFrames(interleaveOrder, primaryFrame, secondaryFrame);
		else if (secondaryFrame != -1)
			model_1.applyTransform(secondaryFrame);
		if (scaleXZ != 128 || scaleY != 128)
			model_1.scale(scaleXZ, scaleXZ, scaleY);
		model_1.calculateDistances();
		model_1.faceGroups = null;
		model_1.vertexGroups = null;
		if (size == 1)
			model_1.fits_on_single_square = true;
		return model_1;
	}

	public void readValues(Buffer buffer) {
	    
	    while(true) {
            int opcode = buffer.readUnsignedByte();
            if (opcode == 0) {
                return;
            } else if (opcode == 1) {
                int len = buffer.readUnsignedByte();
                modelId = new int[len];
                for (int i = 0; i < len; i++) {
                    modelId[i] = buffer.readUShort();
                }
            } else if (opcode == 2) {
                name = buffer.readString();
            } else if (opcode == 12) {
                size = buffer.readUnsignedByte();
            } else if (opcode == 13) {
                standAnim = buffer.readUShort();
            } else if (opcode == 14) {
                walkAnim = buffer.readUShort();
            } else if (opcode == 15) {
                buffer.readUShort();
            } else if (opcode == 16) {
                buffer.readUShort();
            } else if (opcode == 17) {
                walkAnim = buffer.readUShort();
                turn180AnimIndex = buffer.readUShort();
                turn90CWAnimIndex = buffer.readUShort();
                turn90CCWAnimIndex = buffer.readUShort();
                if (turn180AnimIndex == 65535) {
                    turn180AnimIndex = walkAnim;
                }
                if (turn90CWAnimIndex == 65535) {
                    turn90CWAnimIndex = walkAnim;
                }
                if (turn90CCWAnimIndex == 65535) {
                    turn90CCWAnimIndex = walkAnim;
                }
            } else if (opcode >= 30 && opcode < 35) {
                if (actions == null) {
                    actions = new String[5];
                }

                actions[opcode - 30] = buffer.readString();

                if (actions[opcode - 30].equalsIgnoreCase("Hidden")) {
                    actions[opcode - 30] = null;
                }
            } else if (opcode == 40) {
                int len = buffer.readUnsignedByte();
                recolourOriginal = new int[len];
                recolourTarget = new int[len];
                for (int i = 0; i < len; i++) {
                    recolourOriginal[i] = buffer.readUShort();
                    recolourTarget[i] = buffer.readUShort();
                }

            } else if (opcode == 41) {
                int len = buffer.readUnsignedByte();

                for (int i = 0; i < len; i++) {
                    buffer.readUShort(); // textures
                    buffer.readUShort();
                }
            } else if (opcode == 60) {
                int len = buffer.readUnsignedByte();
                additionalModels = new int[len];
                for (int i = 0; i < len; i++) {
                    additionalModels[i] = buffer.readUShort();
                }
            } else if (opcode == 93) {
                drawMinimapDot = false;
            } else if (opcode == 95)
                combatLevel = buffer.readUShort();
            else if (opcode == 97)
                scaleXZ = buffer.readUShort();
            else if (opcode == 98)
                scaleY = buffer.readUShort();
            else if (opcode == 99)
                priorityRender = true;
            else if (opcode == 100)
                lightModifier = buffer.readSignedByte();
            else if (opcode == 101)
                shadowModifier = buffer.readSignedByte();
            else if (opcode == 102)
                headIcon = buffer.readUShort();
            else if (opcode == 103)
                degreesToTurn = buffer.readUShort();
            else if (opcode == 106 || opcode == 118) {
                varBitID = buffer.readUShort();

                if (varBitID == 65535) {
                    varBitID = -1;
                }

                settingId = buffer.readUShort();

                if (settingId == 65535) {
                    settingId = -1;
                }

                int value = -1;

                if (opcode == 118) {
                    value = buffer.readUShort();
                }

                int len = buffer.readUnsignedByte();
                childrenIDs = new int[len + 2];
                for (int i = 0; i <= len; i++) {
                    childrenIDs[i] = buffer.readUShort();
                    if (childrenIDs[i] == 65535) {
                        childrenIDs[i] = -1;
                    }
                }
                childrenIDs[len + 1] = value;
            } else if (opcode == 109) {
                clickable = false;
            } else if (opcode == 107 || opcode == 111) {
                
            } else {
                System.out.println(String.format("npc def invalid opcode: %d", opcode));
            }
        }
	}
}
