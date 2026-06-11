package view.graphic.ui.controller;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import java.util.HashMap;

public class AssetManager {
    private static final HashMap<String, Image> sprites = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadAssets() {
        if (isLoaded) return;

        try {
            System.out.println("🎨 [AssetManager] Bắt đầu nạp tài nguyên và cắt ảnh cỏ theo kích thước 112x48...");

            // =========================================================
            // 1. XỬ LÝ CẮT ẢNH NỀN CỎ CHUẨN KÍCH THƯỚC 112x48
            // =========================================================
            try {
                // Nạp file ảnh gốc Grass.png (112x48 pixel)
                Image fullGrassImg = new Image(AssetManager.class.getResourceAsStream("/image/terrains/grass/Grass.png"));

                // Thuật toán cắt ảnh chính xác:
                // - Toà độ bắt đầu cắt: X = 48, Y = 0 (Bỏ phần ô trống bên trái)
                // - Kích thước lấy: Rộng = 48, Cao = 48 pixel (Lấy trọn vẹn cụm cỏ xanh thuần ở giữa)
                WritableImage croppedGrass = new WritableImage(
                        fullGrassImg.getPixelReader(),
                        48, 0,   // Góc trên bên trái vùng cỏ thuần
                        16, 16   // Kích thước vuông vắn 48x48 pixel
                );

                // Đưa ảnh cỏ đã cắt sạch sẽ vào quản lý
                sprites.put("tile_grass", croppedGrass);
                System.out.println("✂️ [AssetManager] Đã cắt thành công bãi cỏ xanh thuần (48x48)!");

            } catch (Exception e) {
                System.err.println("❌ LỖI: Không thể tìm thấy hoặc cắt ảnh Grass.png!");
                e.printStackTrace();
            }

            // =========================================================
            // 2. NẠP CÁC THÀNH PHẦN ĐỊA HÌNH KHÁC
            // =========================================================
            loadSingleAsset("tree_medium", "/image/terrains/tree/tree.png", "tile_grass");
            loadSingleAsset("tree_small", "/image/terrains/bush/bush.png", "tile_grass");
            loadSingleAsset("tree_big", "/image/terrains/tree/tree.png", "tile_grass");

            // Nạp ảnh nước thật
            loadSingleAsset("tile_water", "/image/terrains/water/water.png", "tile_grass");

            // Các asset sơ cua chống sập
            loadSingleAsset("forest", "/image/terrains/forest/forest.png", "tile_grass");
            loadSingleAsset("stone", "/image/terrains/rock/rock.png", "tree_small");
            loadSingleAsset("tile_stone", "/image/terrains/rock/rock.png", "tree_small");
            loadSingleAsset("tile_dirt", "/image/terrains/grass/dirt.png", "tile_grass");

            // =========================================================
            // 3. NẠP SINH VẬT ĐƠN LẺ
            // =========================================================
            loadSingleAsset("animal_rabbit", "/image/animals/rabbit/rabbit1.png", null);
            loadSingleAsset("animal_elephant", "/image/animals/elephant/elephant.png", "animal_rabbit");
            loadSingleAsset("animal_tiger", "/image/animals/tiger/tiger.png", "animal_rabbit");
            loadSingleAsset("animal_wolf", "/image/animals/wolf/wolf.png", "animal_rabbit");
            loadSingleAsset("animal_fish", "/image/animals/fish/fish.png", "animal_rabbit");
            loadSingleAsset("animal_bear", "/image/animals/bear/bear.png", "animal_tiger");

            sprites.put("animal_deer", sprites.get("animal_rabbit"));
            sprites.put("animal_duck", sprites.get("animal_fish"));

            System.out.println("✅ [AssetManager] Bộ asset mượt mà đã sẵn sàng!");
            isLoaded = true;
        } catch (Exception e) {
            System.err.println("❌ LỖI NGHIÊM TRỌNG: Thiết lập AssetManager thất bại!");
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

    public static Image get(String key) {
        return sprites.get(key);
    }
}