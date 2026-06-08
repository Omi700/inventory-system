USE inventory_mvp;

INSERT INTO product (product_code, product_name, category, unit, safe_stock, status)
VALUES
  ('P009', '云南小粒咖啡豆', '咖啡豆', '袋', 25, 1),
  ('P010', '冷萃咖啡液', '咖啡饮品', '瓶', 40, 1),
  ('P011', '挂耳咖啡礼盒', '咖啡饮品', '盒', 30, 1),
  ('P012', '咖啡搅拌棒', '耗材', '包', 80, 1),
  ('P013', '咖啡杯盖', '耗材', '箱', 60, 1),
  ('P014', '摩卡咖啡杯', '杯具', '个', 35, 1),
  ('P015', '法压壶', '设备', '台', 10, 1),
  ('P016', '停用测试咖啡豆', '咖啡豆', '袋', 20, 0)
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  category = VALUES(category),
  unit = VALUES(unit),
  safe_stock = VALUES(safe_stock),
  status = VALUES(status);
