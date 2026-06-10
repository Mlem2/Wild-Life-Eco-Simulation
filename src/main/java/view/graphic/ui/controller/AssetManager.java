package view.graphic.ui.controller;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import java.util.HashMap;

public class AssetManager {
    private static final HashMap<String, Image> sprites = new HashMap<>();

    public static void loadAssets() {
        try {
            // 1. Nạp ảnh Sinh vật (Animals)
            Image animalsImg = new Image(AssetManager.class.getResourceAsStream("/assets/animals/Basic_Sprites_1x.png"));
            Image rabbitImg = new Image(AssetManager.class.getResourceAsStream("/assets/animals/rabbit.png"));
            

            // 2. Nạp ảnh Địa hình (Terrains)
            Image waterTileImg = new Image(AssetManager.class.getResourceAsStream("/assets/terrains/water/free_water_tile.png"));
            Image pathTileImg = new Image(AssetManager.class.getResourceAsStream("/assets/terrains/ground/free_path_tile.png"));
            Image mineralImg = new Image(AssetManager.class.getResourceAsStream("/assets/terrains/rock/free_minerals.png"));

            // 3. Nạp ảnh Cây tĩnh (Trees)
            sprites.put("tree_small", new Image(AssetManager.class.getResourceAsStream("/assets/terrains/tree/small_oak_tree_static.png")));
            sprites.put("tree_medium", new Image(AssetManager.class.getResourceAsStream("/assets/terrains/tree/medium_oak_tree_static.png")));
            sprites.put("tree_big", new Image(AssetManager.class.getResourceAsStream("/assets/terrains/tree/big_oak_tree_static.png")));

            // --- TRÍCH XUẤT ĐỊA HÌNH (Cắt ô 16x16 pixel từ ảnh lớn) ---
            // Ô cỏ xanh thuần (Góc trên cùng bên trái của file water tile)
            sprites.put("tile_grass", new WritableImage(waterTileImg.getPixelReader(), 0, 0, 16, 16));
            // Ô nước thuần (Hàng thứ 3, cột 1 trong file water tile)
            sprites.put("tile_water", new WritableImage(waterTileImg.getPixelReader(), 0, 32, 16, 16));
            // Ô đất cày/lối đi màu cam (Hàng thứ 3, cột 1 trong file path tile)
            sprites.put("tile_dirt", new WritableImage(pathTileImg.getPixelReader(), 0, 32, 16, 16));
            // Ô khoáng sản thạch anh (Góc trên bên trái file minerals)
            sprites.put("tile_stone", new WritableImage(mineralImg.getPixelReader(), 0, 0, 16, 16));

            // --- TRÍCH XUẤT ĐỘNG VẬT TỪ SPRITESHEET (Mỗi ô 16x16) ---
            sprites.put("animal_duck", new WritableImage(animalsImg.getPixelReader(), 16 * 2, 0, 16, 16));      // Hàng 1, Cột 3
            sprites.put("animal_rabbit", new WritableImage(animalsImg.getPixelReader(), 16 * 1, 16, 16, 16));   // Hàng 2, Cột 2
            sprites.put("animal_deer", new WritableImage(animalsImg.getPixelReader(), 16 * 2, 16, 16, 16));     // Hàng 2, Cột 3 (Dùng gấu làm hươu)
            sprites.put("animal_wolf", new WritableImage(animalsImg.getPixelReader(), 16 * 3, 16, 16, 16));     // Hàng 2, Cột 4
            sprites.put("animal_tiger", new WritableImage(animalsImg.getPixelReader(), 16 * 4, 16, 16, 16));    // Hàng 2, Cột 5
            sprites.put("animal_elephant", new WritableImage(animalsImg.getPixelReader(), 16 * 4, 0, 16, 16)); // Hàng 1, Cột 5 (Dùng cừu làm voi)

            System.out.println("🎨 All Stardew Valley Pixel assets loaded successfully!");
        } catch (Exception e) {
            System.err.println("LỖI NẠP ASSETS: Hãy kiểm tra lại cấu trúc thư mục hoặc tên file!");
            e.printStackTrace();
        }
    }

    public static Image get(String key) {
        return sprites.get(key);
    }
}