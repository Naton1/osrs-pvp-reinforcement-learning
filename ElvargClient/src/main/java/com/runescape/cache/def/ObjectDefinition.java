package com.runescape.cache.def;

import com.runescape.Client;
import com.runescape.cache.FileArchive;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.config.VariableBits;
import com.runescape.collection.ReferenceCache;
import com.runescape.entity.model.Model;
import com.runescape.io.Buffer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class ObjectDefinition {

    public static final Model[] aModelArray741s = new Model[4];
    private static final int[] OBELISK_IDS = {14829, 14830, 14827, 14828, 14826, 14831};
    public static boolean lowMemory;
    public static Buffer stream;
    public static int[] streamIndices;
    public static Client clientInstance;
    public static int cacheIndex;
    public static ReferenceCache models = new ReferenceCache(30);
    public static ObjectDefinition[] cache;
    public static ReferenceCache baseModels = new ReferenceCache(500);
    public static int TOTAL_OBJECTS;
    public boolean obstructsGround;
    public byte ambientLighting;
    public int translateX;
    public String name;
    public int scaleZ;
    public int lightDiffusion;
    public int objectSizeX;
    public int translateY;
    public int minimapFunction;
    public int[] originalModelColors;
    public int scaleX;
    public int varp;
    public boolean inverted;
    public int type;
    public boolean impenetrable;
    public int mapscene;
    public int childrenIDs[];
    public int supportItems;
    public int objectSizeY;
    public boolean contouredGround;
    public boolean occludes;
    public boolean removeClipping;
    public boolean solid;
    public int surroundings;
    public boolean delayShading;
    public int scaleY;
    public int[] modelIds;
    public int varbit;
    public int decorDisplacement;
    public int[] modelTypes;
    public String description;
    public boolean isInteractive;
    public boolean castsShadow;
    public int animation;
    public int translateZ;
    public int[] modifiedModelColors;
    public String interactions[];
    private short[] originalModelTexture;
    private short[] modifiedModelTexture;

    public ObjectDefinition() {
        type = -1;
    }

    public static void dumpNames() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./Cache/object_names.txt"));
        for (int i = 0; i < TOTAL_OBJECTS; i++) {
            ObjectDefinition def = lookup(i);
            String name = def == null ? "null" : def.name;
            writer.write("ID: " + i + ", name: " + name + "");
            writer.newLine();
        }
        writer.close();
    }

    public static ObjectDefinition lookup(int id) {
        if (id > streamIndices.length)
            id = streamIndices.length - 1;
        for (int index = 0; index < 20; index++)
            if (cache[index].type == id)
                return cache[index];

		if (id == 25913)
			id = 15552;

		if (id == 25916 || id == 25926)
			id = 15553;

		if (id == 25917)
			id = 15554;

        cacheIndex = (cacheIndex + 1) % 20;
        ObjectDefinition objectDef = cache[cacheIndex];
        stream.currentPosition = streamIndices[id];
        objectDef.type = id;
        objectDef.reset();
        objectDef.readValues(stream);
        if (objectDef.type > 14500) {
			if (objectDef.delayShading) {
				objectDef.delayShading = false;
			}
		}

        for (int obelisk : OBELISK_IDS) {
            if (id == obelisk) {
                objectDef.interactions = new String[]{"Activate", null, null, null, null};
            }
        }

        if (id == 29241) {
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Restore-stats";
        }
        if (id == 4150) {
            objectDef.name = "Bank portal";
        } else if (id == 4151) {
            objectDef.name = "Ditch portal";
        }

        if (id == 26756) {
            objectDef.name = "Information";
            objectDef.interactions = null;
        }

        if (id == 29150) {
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Venerate";
            objectDef.interactions[1] = "Switch-normal";
            objectDef.interactions[2] = "Switch-ancient";
            objectDef.interactions[3] = "Switch-lunar";
            objectDef.name = "Magical altar";
        }

        if (id == 6552) {
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Toggle-spells";
            objectDef.name = "Ancient altar";
        }

        if (id == 14911) {
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Toggle-spells";
            objectDef.name = "Lunar altar";
        }
        if (id == 2164) {
            objectDef.isInteractive = true;
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Fix";
            objectDef.interactions[1] = null;
            objectDef.name = "Trawler Net";
        }
        if (id == 1293) {
            objectDef.isInteractive = true;
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Teleport";
            objectDef.interactions[1] = null;
            objectDef.name = "Spirit Tree";
        }

        if (id == 2452) {
            objectDef.isInteractive = true;
            objectDef.interactions = new String[5];
            objectDef.interactions[0] = "Go Through";
            objectDef.name = "Passage";
        }
        switch (id) {
            case 10638:
                objectDef.isInteractive = true;
                return objectDef;
        }


        return objectDef;
    }

    public static void clear() {
        baseModels = null;
        models = null;
        streamIndices = null;
        cache = null;
        stream = null;
    }

    public static void init(FileArchive archive) throws IOException {
        stream = new Buffer(archive.readFile("loc.dat"));
        Buffer stream = new Buffer(archive.readFile("loc.idx"));
        TOTAL_OBJECTS = stream.readUShort();
        streamIndices = new int[TOTAL_OBJECTS];
        int offset = 2;
        for (int index = 0; index < TOTAL_OBJECTS; index++) {
            streamIndices[index] = offset;
            offset += stream.readUShort();
        }
        cache = new ObjectDefinition[20];
        for (int index = 0; index < 20; index++)
            cache[index] = new ObjectDefinition();

        System.out.println("Loaded: " + TOTAL_OBJECTS + " objects");
    }

    public void reset() {
        modelIds = null;
        modelTypes = null;
        name = null;
        description = null;
        modifiedModelColors = null;
        originalModelColors = null;
		modifiedModelTexture = null;
		originalModelTexture = null;
        objectSizeX = 1;
        objectSizeY = 1;
        solid = true;
        impenetrable = true;
        isInteractive = false;
        contouredGround = false;
        delayShading = false;
        occludes = false;
        animation = -1;
        decorDisplacement = 16;
        ambientLighting = 0;
        lightDiffusion = 0;
        interactions = null;
        minimapFunction = -1;
        mapscene = -1;
        inverted = false;
        castsShadow = true;
        scaleX = 128;
        scaleY = 128;
        scaleZ = 128;
        surroundings = 0;
        translateX = 0;
        translateY = 0;
        translateZ = 0;
        obstructsGround = false;
        removeClipping = false;
        supportItems = -1;
        varbit = -1;
        varp = -1;
        childrenIDs = null;
    }

    public boolean method577(int i) {
        if (modelTypes == null) {
            if (modelIds == null)
                return true;
            if (i != 10)
                return true;
            boolean flag1 = true;
            for (int k = 0; k < modelIds.length; k++)
                flag1 &= Model.isCached(modelIds[k] & 0xffff);

            return flag1;
        }
        for (int j = 0; j < modelTypes.length; j++)
            if (modelTypes[j] == i)
                return Model.isCached(modelIds[j] & 0xffff);

        return true;
    }

    public Model modelAt(int type, int orientation, int aY, int bY, int cY, int dY, int frameId) {
        Model model = model(type, frameId, orientation);
        if (model == null)
            return null;
        if (contouredGround || delayShading)
            model = new Model(contouredGround, delayShading, model);
        if (contouredGround) {
            int y = (aY + bY + cY + dY) / 4;
            for (int vertex = 0; vertex < model.numVertices; vertex++) {
                int x = model.vertexX[vertex];
                int z = model.vertexZ[vertex];
                int l2 = aY + ((bY - aY) * (x + 64)) / 128;
                int i3 = dY + ((cY - dY) * (x + 64)) / 128;
                int j3 = l2 + ((i3 - l2) * (z + 64)) / 128;
                model.vertexY[vertex] += j3 - y;
            }

            model.computeSphericalBounds();
        }
        return model;
    }

    public boolean method579() {
        if (modelIds == null)
            return true;
        boolean flag1 = true;
        for (int i = 0; i < modelIds.length; i++)
            flag1 &= Model.isCached(modelIds[i] & 0xffff);
        return flag1;
    }

    public ObjectDefinition method580() {
        int i = -1;
        if (varbit != -1) {
            VariableBits varBit = VariableBits.varbits[varbit];
            int j = varBit.getSetting();
            int k = varBit.getLow();
            int l = varBit.getHigh();
            int i1 = Client.BIT_MASKS[l - k];
            i = clientInstance.settings[j] >> k & i1;
        } else if (varp != -1)
            i = clientInstance.settings[varp];
        if (i < 0 || i >= childrenIDs.length || childrenIDs[i] == -1)
            return null;
        else
            return lookup(childrenIDs[i]);
    }

    public Model model(int j, int k, int l) {
        Model model = null;
        long l1;
        if (modelTypes == null) {
            if (j != 10)
                return null;
            l1 = (long) ((type << 6) + l) + ((long) (k + 1) << 32);
            Model model_1 = (Model) models.get(l1);
            if (model_1 != null) {
                return model_1;
            }
            if (modelIds == null)
                return null;
            boolean flag1 = inverted ^ (l > 3);
            int k1 = modelIds.length;
            for (int i2 = 0; i2 < k1; i2++) {
                int l2 = modelIds[i2];
                if (flag1)
                    l2 += 0x10000;
                model = (Model) baseModels.get(l2);
                if (model == null) {
                    model = Model.getModel(l2 & 0xffff);
                    if (model == null)
                        return null;
                    if (flag1)
                        model.method477();
                    baseModels.put(model, l2);
                }
                if (k1 > 1)
                    aModelArray741s[i2] = model;
            }

            if (k1 > 1)
                model = new Model(k1, aModelArray741s);
        } else {
            int i1 = -1;
            for (int j1 = 0; j1 < modelTypes.length; j1++) {
                if (modelTypes[j1] != j)
                    continue;
                i1 = j1;
                break;
            }

            if (i1 == -1)
                return null;
            l1 = (long) ((type << 8) + (i1 << 3) + l) + ((long) (k + 1) << 32);
            Model model_2 = (Model) models.get(l1);
            if (model_2 != null) {
                return model_2;
            }
            if (modelIds == null) {
                return null;
            }
            int j2 = modelIds[i1];
            boolean flag3 = inverted ^ (l > 3);
            if (flag3)
                j2 += 0x10000;
            model = (Model) baseModels.get(j2);
            if (model == null) {
                model = Model.getModel(j2 & 0xffff);
                if (model == null)
                    return null;
                if (flag3)
                    model.method477();
                baseModels.put(model, j2);
            }
        }
        boolean flag;
        flag = scaleX != 128 || scaleY != 128 || scaleZ != 128;
        boolean flag2;
        flag2 = translateX != 0 || translateY != 0 || translateZ != 0;
        Model model_3 = new Model(modifiedModelColors == null,
                Frame.noAnimationInProgress(k), l == 0 && k == -1 && !flag
                && !flag2, modifiedModelTexture == null, model);
        if (k != -1) {
            model_3.skin();
            model_3.applyTransform(k);
            model_3.faceGroups = null;
            model_3.vertexGroups = null;
        }
        while (l-- > 0)
            model_3.rotate90Degrees();
        if (modifiedModelColors != null) {
            for (int k2 = 0; k2 < modifiedModelColors.length; k2++)
                model_3.recolor(modifiedModelColors[k2],
                        originalModelColors[k2]);

        }
        if (modifiedModelTexture != null) {
            for (int k2 = 0; k2 < modifiedModelTexture.length; k2++)
                model_3.retexture(modifiedModelTexture[k2],
                        originalModelTexture[k2]);

        }
        if (flag)
            model_3.scale(scaleX, scaleZ, scaleY);
        if (flag2)
            model_3.translate(translateX, translateY, translateZ);
        model_3.light(85 + ambientLighting, 768 + lightDiffusion, -50, -10, -50, !delayShading);
        if (supportItems == 1)
            model_3.itemDropHeight = model_3.modelBaseY;
        models.put(model_3, l1);
        return model_3;
    }

    public void readValues(Buffer buffer) {
        while(true) {
            int opcode = buffer.readUnsignedByte();

            if (opcode == 0) {
                break;
            } else if (opcode == 1) {
                int len = buffer.readUnsignedByte();
                if (len > 0) {
                    if (modelIds == null) {
                        modelTypes = new int[len];
                        modelIds = new int[len];

                        for (int i = 0; i < len; i++) {
                            modelIds[i] = buffer.readUShort();
                            modelTypes[i] = buffer.readUnsignedByte();
                        }
                    } else {
                        buffer.currentPosition += len * 3;
                    }
                }
            } else if (opcode == 2) {
                name = buffer.readString();
            } else if (opcode == 5) {
                int len = buffer.readUnsignedByte();
                if (len > 0) {
                    if (modelIds == null) {
                        modelTypes = null;
                        modelIds = new int[len];
                        for (int i = 0; i < len; i++) {
                            modelIds[i] = buffer.readUShort();
                        }
                    } else {
                        buffer.currentPosition += len * 2;
                    }
                }
            } else if (opcode == 14) {
                objectSizeX = buffer.readUnsignedByte();
            } else if (opcode == 15) {
                objectSizeY = buffer.readUnsignedByte();
            } else if (opcode == 17) {
                solid = false;
            } else if (opcode == 18) {
                impenetrable = false;
            } else if (opcode == 19) {
                isInteractive = (buffer.readUnsignedByte() == 1);
            } else if (opcode == 21) {
                contouredGround = true;
            } else if (opcode == 22) {
                delayShading = true;
            } else if (opcode == 23) {
                occludes = true;
            } else if (opcode == 24) {
                animation = buffer.readUShort();
                if (animation == 0xFFFF) {
                    animation = -1;
                }
            } else if (opcode == 27) {
                // clipType = 1;
            } else if (opcode == 28) {
                decorDisplacement = buffer.readUnsignedByte();
            } else if (opcode == 29) {
                ambientLighting = buffer.readSignedByte();
            } else if (opcode == 39) {
                lightDiffusion = buffer.readSignedByte() * 25;
            } else if (opcode >= 30 && opcode < 35) {
                if (interactions == null) {
                    interactions = new String[5];
                }
                interactions[opcode - 30] = buffer.readString();
                if (interactions[opcode - 30].equalsIgnoreCase("Hidden")) {
                    interactions[opcode - 30] = null;
                }
            } else if (opcode == 40) {
                int len = buffer.readUnsignedByte();
                modifiedModelColors = new int[len];
                originalModelColors = new int[len];
                for (int i = 0; i < len; i++) {
                    modifiedModelColors[i] = buffer.readUShort();
                    originalModelColors[i] = buffer.readUShort();
                }
            } else if (opcode == 41) {
                int len = buffer.readUnsignedByte();
                modifiedModelTexture = new short[len];
                originalModelTexture = new short[len];
                for (int i = 0; i < len; i++) {
                    modifiedModelTexture[i] = (short) buffer.readUShort();
                    originalModelTexture[i] = (short) buffer.readUShort();
                }

            } else if (opcode == 62) {
                inverted = true;
            } else if (opcode == 64) {
                castsShadow = false;
            } else if (opcode == 65) {
                scaleX = buffer.readUShort();
            } else if (opcode == 66) {
                scaleY = buffer.readUShort();
            } else if (opcode == 67) {
                scaleZ = buffer.readUShort();
            } else if (opcode == 68) {
                mapscene = buffer.readUShort();
            } else if (opcode == 69) {
                surroundings = buffer.readUnsignedByte();
            } else if (opcode == 70) {
                translateX = buffer.readUShort();
            } else if (opcode == 71) {
                translateY = buffer.readUShort();
            } else if (opcode == 72) {
                translateZ = buffer.readUShort();
            } else if (opcode == 73) {
                obstructsGround = true;
            } else if (opcode == 74) {
                removeClipping = true;
            } else if (opcode == 75) {
                supportItems = buffer.readUnsignedByte();
            } else if (opcode == 78) {
                buffer.readUShort(); // ambient sound id
                buffer.readUnsignedByte();
            } else if (opcode == 79) {
                buffer.readUShort();
                buffer.readUShort();
                buffer.readUnsignedByte();
                int len = buffer.readUnsignedByte();

                for (int i = 0; i < len; i++) {
                    buffer.readUShort();
                }
            } else if (opcode == 81) {
                buffer.readUnsignedByte();
            } else if (opcode == 82) {
                minimapFunction = buffer.readUShort();

                if (minimapFunction == 0xFFFF) {
                    minimapFunction = -1;
                }
            } else if (opcode == 77 || opcode == 92) {
                varp = buffer.readUShort();

                if (varp == 0xFFFF) {
                    varp = -1;
                }

                varbit = buffer.readUShort();

                if (varbit == 0xFFFF) {
                    varbit = -1;
                }

                int value = -1;

                if (opcode == 92) {
                    value = buffer.readUShort();

                    if (value == 0xFFFF) {
                        value = -1;
                    }
                }

                int len = buffer.readUnsignedByte();

                childrenIDs = new int[len + 2];
                for (int i = 0; i <= len; ++i) {
                    childrenIDs[i] = buffer.readUShort();
                    if (childrenIDs[i] == 0xFFFF) {
                        childrenIDs[i] = -1;
                    }
                }
                childrenIDs[len + 1] = value;
            } else {
                System.out.println("invalid opcode: " + opcode);
            }

        }

        if (name != null && !name.equals("null")) {
            isInteractive = modelIds != null && (modelTypes == null || modelTypes[0] == 10);
            if (interactions != null)
                isInteractive = true;
        }

        if (removeClipping) {
            solid = false;
            impenetrable = false;
        }

        if (supportItems == -1) {
            supportItems = solid ? 1 : 0;
        }
    }

}