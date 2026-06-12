package view.graphic.ui.controller;

import java.util.HashMap;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class AssetManager {
    private static final HashMap<String, Image> sprites = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadAssets() {
        if (isLoaded) return;

        try {
            System.out.println("🎨 [AssetManager] Bắt đầu nạp tài nguyên hệ thống...");

            // =========================================================
            // 1. XỬ LÝ CẮT ẢNH NỀN CỎ
            // =========================================================
            try {
                Image fullGrassImg = new Image(AssetManager.class.getResourceAsStream("/image/terrains/grass/Grass.png"));
                WritableImage croppedGrass = new WritableImage(
                        fullGrassImg.getPixelReader(),
                        48, 0,
                        48, 48
                );
                sprites.put("tile_grass", croppedGrass);
                System.out.println("[AssetManager] Đã cắt thành công bãi cỏ xanh thuần (48x48)!");
            } catch (Exception e) {
                System.err.println("LỖI: Không thể tìm thấy hoặc cắt ảnh Grass.png!");
            }

            // =========================================================
            // 2. NẠP CÁC THÀNH PHẦN ĐỊA HÌNH KHÁC: cắt cây theo thông số pixel tùy chỉnh của sprite
            // =========================================================
            try {
                Image forestSheet = new Image(AssetManager.class.getResourceAsStream("/assets/terrains/new_terrains/forest.png"));
                PixelReader reader = forestSheet.getPixelReader();

                // 🌲 CÂY VỪA (tree_medium): Cậu mở Paint lên, xem góc trên-bên-trái của cây nằm ở đâu thì điền vào đây
                int mediumX = 480;   // Điền tọa độ X đo được từ Paint vào đây
                int mediumY = 624;   // Điền tọa độ Y đo được từ Paint vào đây
                int mediumW = 96;  // Chiều rộng vùng chọn cây (Ví dụ hệ 48 là 96)
                int mediumH = 144;  // Chiều cao vùng chọn cây (Ví dụ hệ 48 là 96)

                WritableImage treeMedium = new WritableImage(reader, mediumX, mediumY, mediumW, mediumH);
                sprites.put("tree_medium", treeMedium);


                // 🌳 CÂY TO (tree_big): Tương tự, điền tọa độ pixel đo được từ Paint
                int bigX = 480;     // Điền tọa độ X đo được từ Paint vào đây
                int bigY = 432;      // Điền tọa độ Y đo được từ Paint vào đây
                int bigW = 144;    // Chiều rộng vùng chọn cây cổ thụ
                int bigH = 192;    // Chiều cao vùng chọn cây cổ thụ

                WritableImage treeBig = new WritableImage(reader, bigX, bigY, bigW, bigH);
                sprites.put("tree_big", treeBig);


                // 🌿 CÂY NHỎ / CÂY TRẺ (tree_small): dùng sprite cây nhỏ trong forest sheet
                int smallX = 384;  // Điền tọa độ X đo được từ Paint vào đây
                int smallY = 96;    // Điền tọa độ Y đo được từ Paint vào đây
                int smallW = 48;   // Rộng
                int smallH = 48;   // Cao

                WritableImage treeSmall = new WritableImage(reader, smallX, smallY, smallW, smallH);
                sprites.put("tree_small", treeSmall);

                // 🌿 BUSH: cắt từ cùng forest sheet
                int bushX = 672;   // Điền tọa độ X đo được từ Paint vào đây
                int bushY = 384;   // Điền tọa độ Y đo được từ Paint vào đây
                int bushW = 48;    // Rộng
                int bushH = 48;    // Cao

                WritableImage bushSprite = new WritableImage(reader, bushX, bushY, bushW, bushH);
                sprites.put("bush", bushSprite);

                System.out.println("🌲 [AssetManager] Đã nạp các cây và bụi theo thông số tùy chỉnh pixel của cậu!");

            } catch (Exception e) {
                System.err.println("❌ Lỗi khi cắt cây tùy chỉnh: " + e.getMessage());
            }

            loadSeasonalTerrainTiles();
            loadDecorationAssets();

            //loadSingleAsset("tile_water", "/image/terrains/water/water.png", "tile_grass");
            //loadSingleAsset("forest", "/assets/terrains/new_terrains/forest.png", "tile_grass");
            loadSingleAsset("stone", "/image/terrains/rock/rock.png", "tree_small");
            //loadSingleAsset("tile_stone", "/image/terrains/rock/rock.png", "tree_small");
            //loadSingleAsset("tile_dirt", "/image/terrains/grass/dirt.png", "tile_grass");

            // =========================================================
            // 3. NẠP VÀ TỰ ĐỘNG CẮT LƯỚI SPRITE SHEET ĐỘNG VẬT (4 HƯỚNG)
            // =========================================================
            loadGridSpriteSheetAnimal("rabbit", "/assets/animals/bunny.png");
            loadGridSpriteSheetAnimal("wolf", "/assets/animals/wolf.png");
            loadGridSpriteSheetAnimal("tiger", "/assets/animals/tiger.png");
            loadGridSpriteSheetAnimal("fish", "/assets/animals/fish.png");
            loadGridSpriteSheetAnimal("elephant", "/assets/animals/elephant.png");

            // Đăng ký ảnh đứng yên mặc định (Lấy hướng nhìn xuống dưới 'down', khung hình số 0)
            if (sprites.containsKey("rabbit_walk_down_0")) sprites.put("animal_rabbit", sprites.get("rabbit_walk_down_0"));
            if (sprites.containsKey("wolf_walk_down_0"))   sprites.put("animal_wolf", sprites.get("wolf_walk_down_0"));
            if (sprites.containsKey("tiger_walk_down_0"))  sprites.put("animal_tiger", sprites.get("tiger_walk_down_0"));
            if (sprites.containsKey("fish_walk_down_0"))   sprites.put("animal_fish", sprites.get("fish_walk_down_0"));
            if (sprites.containsKey("elephant_walk_down_0"))   sprites.put("animal_elephant", sprites.get("elephant_walk_down_0"));

            // Nạp các con thú chỉ có ảnh tĩnh đơn lẻ
            loadSingleAsset("animal_bear", "/image/animals/bear/bear.png", "tile_grass");

            // Các con thú sơ cua
            if (sprites.containsKey("animal_rabbit")) sprites.put("animal_deer", sprites.get("animal_rabbit"));
            if (sprites.containsKey("animal_fish"))   sprites.put("animal_duck", sprites.get("animal_fish"));

            System.out.println("[AssetManager] Toàn bộ Sprite Sheet chuyển động đã sẵn sàng!");
            isLoaded = true;
        } catch (Exception e) {
            System.err.println(" LỖI NGHIÊM TRỌNG: Thiết lập AssetManager thất bại!");
            e.printStackTrace();
        }
    }

    private static void loadSingleAsset(String key, String path, String backupKey) {
        try {
            java.io.InputStream is = AssetManager.class.getResourceAsStream(path);
            if (is != null) {
                sprites.put(key, new Image(is));
            } else {
                throw new Exception("File không tồn tại: " + path);
            }
        } catch (Exception e) {
            if (backupKey != null && sprites.containsKey(backupKey)) {
                sprites.put(key, sprites.get(backupKey));
            }
        }
    }

    /**
     * Hàm tự động cắt ma trận ảnh (4 hướng x 4 khung hình) thành các sprite riêng lẻ
     * @param baseKey Tên gốc (ví dụ: "rabbit")
     * @param path Đường dẫn ảnh
     */
    private static void loadGridSpriteSheetAnimal(String baseKey, String path) {
        try {
            java.io.InputStream is = AssetManager.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Không tìm thấy file ảnh: " + path);
                return;
            }

            Image sheetImage = new Image(is);
            PixelReader reader = sheetImage.getPixelReader();

            // Mặc định các bộ asset pixel của cậu có kích thước ô đơn là 32x32 pixel
            int size = 48;

            // Định nghĩa mảng tên hướng tương ứng với 4 hàng từ trên xuống dưới trong ảnh của cậu
            String[] directions = {"down", "left", "right", "up"};

            // Vòng lặp duyệt qua 4 hàng (4 hướng)
            for (int row = 0; row < 4; row++) {
                String dirName = directions[row];

                // Vòng lặp duyệt qua 4 cột (3 khung hình bước đi)
                for (int col = 0; col < 3; col++) {
                    int startX = col * size; // Tọa độ X bắt đầu cắt
                    int startY = row * size; // Tọa độ Y bắt đầu cắt

                    WritableImage frame = new WritableImage(reader, startX, startY, size, size);

                    // Lưu vào map với key dạng: "rabbit_walk_down_0", "rabbit_walk_right_2"...
                    sprites.put(baseKey + "_walk_" + dirName + "_" + col, frame);
                }
            }
            System.out.println("[Cắt lưới ảnh] Thành công 4 hướng cho: " + baseKey);
        } catch (Exception e) {
            System.err.println("Lỗi khi cắt lưới ảnh " + path + ": " + e.getMessage());
        }
    }

    private static void loadSeasonalTerrainTiles() {
        // Template seasonal: chỉ đổi file path mùa, vị trí crop cố định.
        // Spring và summer dùng chung file MapSpringSummer.
        loadSeasonalTerrain("grass", new String[]{
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapWinter.png"
        }, 408, 72, 48, 48);

        loadSeasonalTerrain("forest", new String[]{
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapWinter.png"
        }, 600, 84, 48, 48);

        loadSeasonalTerrain("water", new String[]{
                "/assets/terrains/new_terrains/water.png",
                "/assets/terrains/new_terrains/water.png",
                "/assets/terrains/new_terrains/water.png",
                "/assets/terrains/new_terrains/water.png"
        }, 408, 72, 48, 48);

        loadSeasonalTerrain("mud", new String[]{
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapAutumn.png"
        }, 408, 216, 48, 48);

        loadSeasonalTerrain("mountain", new String[]{
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapSpringSummer.png",
                "/assets/terrains/new_terrains/MapAutumn.png",
                "/assets/terrains/new_terrains/MapWinter.png"
        }, 504, 216, 48, 48);
    }

    private static void loadSeasonalTerrain(String terrainName, String[] seasonPaths, int cropX, int cropY, int cropW, int cropH) {
        String[] seasons = {"spring", "summer", "autumn", "winter"};
        for (int i = 0; i < Math.min(seasons.length, seasonPaths.length); i++) {
            try {
                java.io.InputStream is = AssetManager.class.getResourceAsStream(seasonPaths[i]);
                if (is == null) {
                    throw new Exception("File không tồn tại: " + seasonPaths[i]);
                }
                Image sheetImage = new Image(is);
                PixelReader reader = sheetImage.getPixelReader();
                WritableImage tile = new WritableImage(reader, cropX, cropY, cropW, cropH);
                sprites.put("tile_" + terrainName + "_" + seasons[i], tile);
            } catch (Exception e) {
                System.err.println("[AssetManager] Không thể nạp seasonal terrain '" + terrainName + "' mùa " + seasons[i] + ": " + e.getMessage());
            }
        }
    }

    private static void loadDecorationAssets() {
        // Template decoration: bạn sửa đường dẫn và vị trí crop theo sprite sheet của mình.
        // Mục tiêu: thêm các lớp overlay nhỏ như cỏ, hoa, đá nhỏ, lá rụng... để chồng lên tile.
        try {
            Image decorSheet = new Image(AssetManager.class.getResourceAsStream("/assets/terrains/new_terrains/forest.png"));
            PixelReader reader = decorSheet.getPixelReader();

            // Ví dụ: cắt 4decor khác nhau từ cùng sprite sheet.
            sprites.put("decor_grass_clump", new WritableImage(reader, 624, 96, 48, 48));
            sprites.put("decor_flower_white", new WritableImage(reader, 480, 96, 48, 48));
            sprites.put("decor_mushroom", new WritableImage(reader, 528, 144, 48, 48));
            sprites.put("decor_leaf_pile", new WritableImage(reader, 528, 192, 48, 48));

            // Nếu cậu muốn dùng nhiều file riêng thì gọi loadSingleAsset với key "decor_*".
            // loadSingleAsset("decor_grass_clump", "/assets/terrains/decorations/grass_clump.png", null);

            System.out.println("[AssetManager] Đã nạp template decoration sprite.");
        } catch (Exception e) {
            System.err.println("[AssetManager] Không thể nạp decoration sprite: " + e.getMessage());
        }
    }

    public static Image get(String key) {
        return sprites.get(key);
    }
}