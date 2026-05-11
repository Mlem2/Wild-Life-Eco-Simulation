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

Branch này tạo ra nhằm so sánh cách suy nghĩ + báo cáo các biến / hành vi cần thêm vào code khác (ví dụ như Animal.java cần cập nhật thêm tuổi/ cd sinh sản như thế nào). Không merge branch này. Chưa hoàn thiện.
