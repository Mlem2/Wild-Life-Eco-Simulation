STRATEGY UPDATE ver 0.3.1\
**Fixed Bug**
- Bụi (Bush) và Cây (Trees) chỉ còn là "Thức ăn" cho Apex herbivores (chủ yếu Voi). Các con vật khác sẽ không ăn loại thức ăn này.
- Tối ưu tạo map
- Tìm đường tốt hơn cho động vật: Có thể tìm nguồn nước gần nhất nếu khát.
- Chủ động chuyển đổi trạng thái tốt hơn: Trong bán kính quét nếu tìm được động vật ăn được sẽ tăng tốc và săn lùng, kể cả trong PriorityStrategy.

**Found Bug**
- Thỏ bị ăn quá nhanh, số lượng giảm rất nhanh chỉ sau một ngày.
-> Nên xử lý nhanh khâu sinh sản & Tăng tốc thời gian (Ví dụ 1 ngày chỉ còn 24 * 2 = 48 tick, tương đương với 24h, 2 tick / 1h), hoặc đơn giản là cho chúng nó đói lâu hơn (1 ngày chỉ cần bị đói 1 hoặc 2 lần).
- Một vài con cá bị kẹt gần hồ nước (nó định lên bờ nhưng code bắt nó ở dưới nước, khi này CD sẽ có thể chạy xuống âm, nói chung ảnh hưởng không đáng kể)

**FLOW**\
<img width="327" height="443" alt="image" src="https://github.com/user-attachments/assets/ef3ca91d-c30c-4c19-b7ef-d217f10d7a3f" />
  \
**Upcoming update**
- Sinh sản và tìm bạn đời.
- Thêm khả năng trốn bằng bụi cây. Nếu kẻ địch quá gần thì có khả năng (nhỏ) bị phát hiện.
  (Còn nữa)
  
================================\
mới clone/pull thì chạy lệnh này
.\mvnw.cmd clean install (win)

Làm cái j cũng tạo branch r mới merge vào main ko được đẩy thằng lên main đâu nhá mấy cu
\
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
