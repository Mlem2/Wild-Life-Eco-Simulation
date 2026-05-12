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

Flow: Động vật scan xung quanh xem có mục tiêu lạ -> Controller điều chỉnh, lựa chọn, ưu tiên hành vi dựa theo thông tin nhận được + giá trị độ đói, khát, etc,... -> Thực hiện strategy, di chuyển tương ứng với hành vi đang có.
