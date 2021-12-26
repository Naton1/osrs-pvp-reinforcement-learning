package com.runescape.scene;

public final class CollisionMap {

    private static final int BLOCKED_TILE = 0x200000;
    //private static final int OBJECT_TILE = 0x100;
    public final int[][] clipData;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;

    public CollisionMap() {
        xOffset = 0;
        yOffset = 0;
        width = 104;
        height = 104;
        clipData = new int[width][height];
        setDefault();
    }

    public void setDefault() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++)
                if (x == 0 || y == 0 || x == width - 1
                        || y == height - 1)
                    clipData[x][y] = 0xffffff;
                else
                    clipData[x][y] = 0x1000000;
        }

    }

    public void method211(int y, int orientation, int x, int group, boolean flag) {
        x -= xOffset;
        y -= yOffset;
        if (group == 0) {
            if (orientation == 0) {
                flag(x, y, 128);
                flag(x - 1, y, 8);
            }
            if (orientation == 1) {
                flag(x, y, 2);
                flag(x, y + 1, 32);
            }
            if (orientation == 2) {
                flag(x, y, 8);
                flag(x + 1, y, 128);
            }
            if (orientation == 3) {
                flag(x, y, 32);
                flag(x, y - 1, 2);
            }
        }
        if (group == 1 || group == 3) {
            if (orientation == 0) {
                flag(x, y, 1);
                flag(x - 1, y + 1, 16);
            }
            if (orientation == 1) {
                flag(x, y, 4);
                flag(x + 1, y + 1, 64);
            }
            if (orientation == 2) {
                flag(x, y, 16);
                flag(x + 1, y - 1, 1);
            }
            if (orientation == 3) {
                flag(x, y, 64);
                flag(x - 1, y - 1, 4);
            }
        }
        if (group == 2) {
            if (orientation == 0) {
                flag(x, y, 130);
                flag(x - 1, y, 8);
                flag(x, y + 1, 32);
            }
            if (orientation == 1) {
                flag(x, y, 10);
                flag(x, y + 1, 32);
                flag(x + 1, y, 128);
            }
            if (orientation == 2) {
                flag(x, y, 40);
                flag(x + 1, y, 128);
                flag(x, y - 1, 2);
            }
            if (orientation == 3) {
                flag(x, y, 160);
                flag(x, y - 1, 2);
                flag(x - 1, y, 8);
            }
        }
        if (flag) {
            if (group == 0) {
                if (orientation == 0) {
                    flag(x, y, 0x10000);
                    flag(x - 1, y, 4096);
                }
                if (orientation == 1) {
                    flag(x, y, 1024);
                    flag(x, y + 1, 16384);
                }
                if (orientation == 2) {
                    flag(x, y, 4096);
                    flag(x + 1, y, 0x10000);
                }
                if (orientation == 3) {
                    flag(x, y, 16384);
                    flag(x, y - 1, 1024);
                }
            }
            if (group == 1 || group == 3) {
                if (orientation == 0) {
                    flag(x, y, 512);
                    flag(x - 1, y + 1, 8192);
                }
                if (orientation == 1) {
                    flag(x, y, 2048);
                    flag(x + 1, y + 1, 32768);
                }
                if (orientation == 2) {
                    flag(x, y, 8192);
                    flag(x + 1, y - 1, 512);
                }
                if (orientation == 3) {
                    flag(x, y, 32768);
                    flag(x - 1, y - 1, 2048);
                }
            }
            if (group == 2) {
                if (orientation == 0) {
                    flag(x, y, 0x10400);
                    flag(x - 1, y, 4096);
                    flag(x, y + 1, 16384);
                }
                if (orientation == 1) {
                    flag(x, y, 5120);
                    flag(x, y + 1, 16384);
                    flag(x + 1, y, 0x10000);
                }
                if (orientation == 2) {
                    flag(x, y, 20480);
                    flag(x + 1, y, 0x10000);
                    flag(x, y - 1, 1024);
                }
                if (orientation == 3) {
                    flag(x, y, 0x14000);
                    flag(x, y - 1, 1024);
                    flag(x - 1, y, 4096);
                }
            }
        }
    }

    public void method212(boolean flag, int j, int k, int l, int i1, int j1) {
        int k1 = 256;
        if (flag)
            k1 += 0x20000;
        l -= xOffset;
        i1 -= yOffset;
        if (j1 == 1 || j1 == 3) {
            int l1 = j;
            j = k;
            k = l1;
        }
        for (int i2 = l; i2 < l + j; i2++)
            if (i2 >= 0 && i2 < width) {
                for (int j2 = i1; j2 < i1 + k; j2++)
                    if (j2 >= 0 && j2 < height)
                        flag(i2, j2, k1);

            }

    }

    public void block(int x, int y) {
        x -= xOffset;
        y -= yOffset;
        clipData[x][y] |= BLOCKED_TILE;
    }

    private void flag(int x, int y, int value) {
        clipData[x][y] |= value;
    }

    public void removeObject(int i, int j, boolean flag, int k, int l) {
        k -= xOffset;
        l -= yOffset;
        if (j == 0) {
            if (i == 0) {
                method217(128, k, l);
                method217(8, k - 1, l);
            }
            if (i == 1) {
                method217(2, k, l);
                method217(32, k, l + 1);
            }
            if (i == 2) {
                method217(8, k, l);
                method217(128, k + 1, l);
            }
            if (i == 3) {
                method217(32, k, l);
                method217(2, k, l - 1);
            }
        }
        if (j == 1 || j == 3) {
            if (i == 0) {
                method217(1, k, l);
                method217(16, k - 1, l + 1);
            }
            if (i == 1) {
                method217(4, k, l);
                method217(64, k + 1, l + 1);
            }
            if (i == 2) {
                method217(16, k, l);
                method217(1, k + 1, l - 1);
            }
            if (i == 3) {
                method217(64, k, l);
                method217(4, k - 1, l - 1);
            }
        }
        if (j == 2) {
            if (i == 0) {
                method217(130, k, l);
                method217(8, k - 1, l);
                method217(32, k, l + 1);
            }
            if (i == 1) {
                method217(10, k, l);
                method217(32, k, l + 1);
                method217(128, k + 1, l);
            }
            if (i == 2) {
                method217(40, k, l);
                method217(128, k + 1, l);
                method217(2, k, l - 1);
            }
            if (i == 3) {
                method217(160, k, l);
                method217(2, k, l - 1);
                method217(8, k - 1, l);
            }
        }
        if (flag) {
            if (j == 0) {
                if (i == 0) {
                    method217(0x10000, k, l);
                    method217(4096, k - 1, l);
                }
                if (i == 1) {
                    method217(1024, k, l);
                    method217(16384, k, l + 1);
                }
                if (i == 2) {
                    method217(4096, k, l);
                    method217(0x10000, k + 1, l);
                }
                if (i == 3) {
                    method217(16384, k, l);
                    method217(1024, k, l - 1);
                }
            }
            if (j == 1 || j == 3) {
                if (i == 0) {
                    method217(512, k, l);
                    method217(8192, k - 1, l + 1);
                }
                if (i == 1) {
                    method217(2048, k, l);
                    method217(32768, k + 1, l + 1);
                }
                if (i == 2) {
                    method217(8192, k, l);
                    method217(512, k + 1, l - 1);
                }
                if (i == 3) {
                    method217(32768, k, l);
                    method217(2048, k - 1, l - 1);
                }
            }
            if (j == 2) {
                if (i == 0) {
                    method217(0x10400, k, l);
                    method217(4096, k - 1, l);
                    method217(16384, k, l + 1);
                }
                if (i == 1) {
                    method217(5120, k, l);
                    method217(16384, k, l + 1);
                    method217(0x10000, k + 1, l);
                }
                if (i == 2) {
                    method217(20480, k, l);
                    method217(0x10000, k + 1, l);
                    method217(1024, k, l - 1);
                }
                if (i == 3) {
                    method217(0x14000, k, l);
                    method217(1024, k, l - 1);
                    method217(4096, k - 1, l);
                }
            }
        }
    }

    public void removeObject(int i, int j, int k, int l, int i1, boolean flag) {
        int j1 = 256;
        if (flag)
            j1 += 0x20000;
        k -= xOffset;
        l -= yOffset;
        if (i == 1 || i == 3) {
            int k1 = j;
            j = i1;
            i1 = k1;
        }
        for (int l1 = k; l1 < k + j; l1++)
            if (l1 >= 0 && l1 < width) {
                for (int i2 = l; i2 < l + i1; i2++)
                    if (i2 >= 0 && i2 < height)
                        method217(j1, l1, i2);

            }

    }

    private void method217(int i, int j, int k) {
        clipData[j][k] &= 0xffffff - i;
    }

    public void removeFloorDecoration(int j, int k) {
        k -= xOffset;
        j -= yOffset;
        clipData[k][j] &= 0xdfffff;
    }

    public boolean method219(int i, int j, int k, int i1, int j1, int k1) {
        if (j == i && k == k1)
            return true;
        j -= xOffset;
        k -= yOffset;
        i -= xOffset;
        k1 -= yOffset;
        if (j1 == 0)
            if (i1 == 0) {
                if (j == i - 1 && k == k1)
                    return true;
                if (j == i && k == k1 + 1
                        && (clipData[j][k] & 0x1280120) == 0)
                    return true;
                if (j == i && k == k1 - 1
                        && (clipData[j][k] & 0x1280102) == 0)
                    return true;
            } else if (i1 == 1) {
                if (j == i && k == k1 + 1)
                    return true;
                if (j == i - 1 && k == k1
                        && (clipData[j][k] & 0x1280108) == 0)
                    return true;
                if (j == i + 1 && k == k1
                        && (clipData[j][k] & 0x1280180) == 0)
                    return true;
            } else if (i1 == 2) {
                if (j == i + 1 && k == k1)
                    return true;
                if (j == i && k == k1 + 1
                        && (clipData[j][k] & 0x1280120) == 0)
                    return true;
                if (j == i && k == k1 - 1
                        && (clipData[j][k] & 0x1280102) == 0)
                    return true;
            } else if (i1 == 3) {
                if (j == i && k == k1 - 1)
                    return true;
                if (j == i - 1 && k == k1
                        && (clipData[j][k] & 0x1280108) == 0)
                    return true;
                if (j == i + 1 && k == k1
                        && (clipData[j][k] & 0x1280180) == 0)
                    return true;
            }
        if (j1 == 2)
            if (i1 == 0) {
                if (j == i - 1 && k == k1)
                    return true;
                if (j == i && k == k1 + 1)
                    return true;
                if (j == i + 1 && k == k1
                        && (clipData[j][k] & 0x1280180) == 0)
                    return true;
                if (j == i && k == k1 - 1
                        && (clipData[j][k] & 0x1280102) == 0)
                    return true;
            } else if (i1 == 1) {
                if (j == i - 1 && k == k1
                        && (clipData[j][k] & 0x1280108) == 0)
                    return true;
                if (j == i && k == k1 + 1)
                    return true;
                if (j == i + 1 && k == k1)
                    return true;
                if (j == i && k == k1 - 1
                        && (clipData[j][k] & 0x1280102) == 0)
                    return true;
            } else if (i1 == 2) {
                if (j == i - 1 && k == k1
                        && (clipData[j][k] & 0x1280108) == 0)
                    return true;
                if (j == i && k == k1 + 1
                        && (clipData[j][k] & 0x1280120) == 0)
                    return true;
                if (j == i + 1 && k == k1)
                    return true;
                if (j == i && k == k1 - 1)
                    return true;
            } else if (i1 == 3) {
                if (j == i - 1 && k == k1)
                    return true;
                if (j == i && k == k1 + 1
                        && (clipData[j][k] & 0x1280120) == 0)
                    return true;
                if (j == i + 1 && k == k1
                        && (clipData[j][k] & 0x1280180) == 0)
                    return true;
                if (j == i && k == k1 - 1)
                    return true;
            }
        if (j1 == 9) {
            if (j == i && k == k1 + 1 && (clipData[j][k] & 0x20) == 0)
                return true;
            if (j == i && k == k1 - 1 && (clipData[j][k] & 2) == 0)
                return true;
            if (j == i - 1 && k == k1 && (clipData[j][k] & 8) == 0)
                return true;
            if (j == i + 1 && k == k1 && (clipData[j][k] & 0x80) == 0)
                return true;
        }
        return false;
    }

    public boolean method220(int i, int j, int k, int l, int i1, int j1) {
        if (j1 == i && k == j)
            return true;
        j1 -= xOffset;
        k -= yOffset;
        i -= xOffset;
        j -= yOffset;
        if (l == 6 || l == 7) {
            if (l == 7)
                i1 = i1 + 2 & 3;
            if (i1 == 0) {
                if (j1 == i + 1 && k == j && (clipData[j1][k] & 0x80) == 0)
                    return true;
                if (j1 == i && k == j - 1 && (clipData[j1][k] & 2) == 0)
                    return true;
            } else if (i1 == 1) {
                if (j1 == i - 1 && k == j && (clipData[j1][k] & 8) == 0)
                    return true;
                if (j1 == i && k == j - 1 && (clipData[j1][k] & 2) == 0)
                    return true;
            } else if (i1 == 2) {
                if (j1 == i - 1 && k == j && (clipData[j1][k] & 8) == 0)
                    return true;
                if (j1 == i && k == j + 1 && (clipData[j1][k] & 0x20) == 0)
                    return true;
            } else if (i1 == 3) {
                if (j1 == i + 1 && k == j && (clipData[j1][k] & 0x80) == 0)
                    return true;
                if (j1 == i && k == j + 1 && (clipData[j1][k] & 0x20) == 0)
                    return true;
            }
        }
        if (l == 8) {
            if (j1 == i && k == j + 1 && (clipData[j1][k] & 0x20) == 0)
                return true;
            if (j1 == i && k == j - 1 && (clipData[j1][k] & 2) == 0)
                return true;
            if (j1 == i - 1 && k == j && (clipData[j1][k] & 8) == 0)
                return true;
            if (j1 == i + 1 && k == j && (clipData[j1][k] & 0x80) == 0)
                return true;
        }
        return false;
    }

    public boolean atObject(int finalY, int finalX, int x, int height, int rotation, int width, int y) {
        int maxX = (finalX + width) - 1;
        int maxY = (finalY + height) - 1;
        if (x >= finalX && x <= maxX && y >= finalY && y <= maxY)
            return true;
        if (x == finalX - 1 && y >= finalY && y <= maxY
                && (clipData[x - xOffset][y - yOffset] & 8) == 0
                && (rotation & 8) == 0)
            return true;
        if (x == maxX + 1 && y >= finalY && y <= maxY
                && (clipData[x - xOffset][y - yOffset] & 0x80) == 0
                && (rotation & 2) == 0)
            return true;
        return y == finalY - 1 && x >= finalX && x <= maxX
                && (clipData[x - xOffset][y - yOffset] & 2) == 0
                && (rotation & 4) == 0 || y == maxY + 1 && x >= finalX && x <= maxX
                && (clipData[x - xOffset][y - yOffset] & 0x20) == 0
                && (rotation & 1) == 0;
    }
}
