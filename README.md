STRATEGY UPDATE ver 0.3.0
<Found Bug>
- Bụi (Bush) và Cây (Trees) đang là "Thức ăn".
- Động vật ăn thịt bằng 1 cách nào đó cũng có thể ăn cỏ ??
- Thanh đói và khát bị giảm khá nhanh.
- Có chút giật lag khi mới tạo thế giới.
- Khả năng chuyển đổi trạng thái chưa linh hoạt (giả sử ưu tiên uống nước nhưng tự nhiên bị dí thế là chạy mất).
<Feature Update>
- Thêm một loạt strategy mới.
- Tìm đường và lấy tầm nhìn theo Chunk.
- Cách vận hành: ChooseTarget --> Chọn Strategy tương ứng --> Chọn mục tiêu di chuyển tới (vị trí) --> Tìm đường Pathfinder tới vị trí mục tiêu. 
- Khi ăn sẽ hồi thanh đói và thanh khát.
- Sửa lại tốc độ di chuyển và thời gian (nên để 1h/ tick thay vì 5 phút).

  <FLOW>\
<img width="327" height="443" alt="image" src="https://github.com/user-attachments/assets/ef3ca91d-c30c-4c19-b7ef-d217f10d7a3f" />
  \
<Upcoming update>
- Sửa lại cơ chế ăn: Ăn đúng, ăn đủ. Biết ăn cỏ và uống nước khi cần thiết.
- Sửa lại khả năng tìm đường của con vật. Ưu tiên di chuyển: Lẩn trốn kẻ thù (Khi ở gần) --> Né xa khỏi kẻ thù (Khi ở xa) --> Tìm đến thức ăn.
- Sửa lại cơ chế bứt tốc và tiêu hao năng lượng.
- Sửa lại bụi và cây giờ đây không còn là thức ăn nữa.
- Thêm khả năng trốn bằng bụi cây. Nếu kẻ địch quá gần thì có khả năng (nhỏ) bị phát hiện.
  (Còn nữa)
  
================================
mới clone/pull thì chạy lệnh này
.\mvnw.cmd clean install (win)

Làm cái j cũng tạo branch r mới merge vào main ko được đẩy thằng lên main đâu nhá mấy cu

src
  main
    java
      brain (strategy con vật eg: huntingStrategy)
      core (map, simulation, nói chung là mấy cái để làm mọi thứ hoạt động)
      entities (động vât)
      view (UI)
    resources (ảnh, âm thanh)
  test
    java (tests)
