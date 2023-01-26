package com.elvarg.game.definition;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.elvarg.game.GameConstants;
import com.elvarg.game.collision.Buffer;
import com.elvarg.util.FileUtil;
import com.elvarg.util.ObjectIdentifiers;

public final class ObjectDefinition extends ObjectIdentifiers {

    private static final int[] OBELISK_IDS = {14829, 14830, 14827, 14828, 14826, 14831};
    public static boolean lowMemory;
    public static Buffer stream;
    public static int[] streamIndices;
    public static int cacheIndex;
    public static ObjectDefinition[] cache;
    public static int totalObjects;
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
    public int id;
    public boolean impenetrable;
    public int mapscene;
    public int childrenIDs[];
    public int supportItems;
    public int objectSizeY;
    public boolean contouredGround;
    public boolean occludes;
    public boolean removeClipping;
    public boolean solid;
    public int blockingMask;
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
    public int clipType = 2;
    

    public ObjectDefinition() {
        id = -1;
    }

    public static void dumpNames() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./Cache/object_names.txt"));
        for (int i = 0; i < totalObjects; i++) {
            ObjectDefinition def = forId(i);
            String name = def == null ? "null" : def.name;
            writer.write("ID: " + i + ", name: " + name + "");
            writer.newLine();
        }
        writer.close();
    }
    public boolean isClippedDecoration() {
        return isInteractive || clipType == 1 || obstructsGround;
    }


    public static ObjectDefinition forId(int id) {
        if (id > streamIndices.length)
            id = streamIndices.length - 1;
        for (int index = 0; index < 20; index++)
            if (cache[index].id == id)
                return cache[index];

        if (id == 25913)
            id = 15552;

        if (id == 25916 || id == 25926)
            id = 15553;

        if (id == 25917)
            id = 15554;

        cacheIndex = (cacheIndex + 1) % 20;
        ObjectDefinition objectDef = cache[cacheIndex];
        stream.offset = streamIndices[id];
        objectDef.id = id;
        objectDef.reset();
        objectDef.readValues(stream);
        if (objectDef.id > 14500) {
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

        if (id == 6552) {
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

    public static void init() {
        try {
            byte[] dat = FileUtil.readFile(GameConstants.CLIPPING_DIRECTORY + "loc.dat");
            byte[] idx = FileUtil.readFile(GameConstants.CLIPPING_DIRECTORY + "loc.idx");

            stream = new Buffer(dat);
            Buffer idxBuffer525 = new Buffer(idx);

            int totalObjects525 = idxBuffer525.readUnsignedWord();
            streamIndices = new int[totalObjects525];
            int i = 2;
            for (int j = 0; j < totalObjects525; j++) {
                streamIndices[j] = i;
                i += idxBuffer525.readUnsignedWord();
            }

            cache = new ObjectDefinition[20];
            for (int k = 0; k < 20; k++) {
                cache[k] = new ObjectDefinition();
            }

            //		+ totalObjects667 + " cache object definitions #667 in " + (System.currentTimeMillis() - startup) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        blockingMask = 0;
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

    void readValues(Buffer buffer) {
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
                        buffer.offset += len * 3;
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
                        buffer.offset += len * 2;
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
                //clipType = 1;
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
                blockingMask = buffer.readUnsignedByte();
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

    public String getName() {
        return name;
    }

    public int getSizeX() {
        return objectSizeX;
    }

    public int getSizeY() {
        return objectSizeY;
    }

    public boolean hasActions() {
        return isInteractive;
    }
    
    public int getSize() {
    	switch (id) {
    	case BARROWS_STAIRCASE_AHRIM:
    	case BARROWS_STAIRCASE_DHAROK:
    	case BARROWS_STAIRCASE_GUTHAN:
    	case BARROWS_STAIRCASE_KARIL:
    	case BARROWS_STAIRCASE_VERAC:
    		return 2;
    	case BARROWS_STAIRCASE_TORAG:
    		return 3;
    	}
    	    	
    	return (getSizeX() + getSizeY()) - 1;
    }
}