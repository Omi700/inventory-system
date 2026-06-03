CREATE DATABASE IF NOT EXISTS inventory_mvp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE inventory_mvp;

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  role VARCHAR(30) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_app_user_username (username),
  KEY idx_app_user_role (role),
  KEY idx_app_user_status (status),
  CONSTRAINT chk_app_user_role CHECK (role IN ('ADMIN', 'WAREHOUSE_ADMIN')),
  CONSTRAINT chk_app_user_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(50) NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  unit VARCHAR(20) NOT NULL,
  safe_stock INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_code (product_code),
  KEY idx_product_name (product_name),
  KEY idx_product_category (category),
  KEY idx_product_status (status),
  CONSTRAINT chk_product_safe_stock_non_negative CHECK (safe_stock >= 0),
  CONSTRAINT chk_product_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_main TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_product_image_product_id (product_id),
  KEY idx_product_image_main (product_id, is_main),
  CONSTRAINT fk_product_image_product
    FOREIGN KEY (product_id) REFERENCES product(id)
    ON DELETE CASCADE,
  CONSTRAINT chk_product_image_main CHECK (is_main IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS warehouse (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_code VARCHAR(50) NOT NULL,
  warehouse_name VARCHAR(100) NOT NULL,
  city VARCHAR(50) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_warehouse_code (warehouse_code),
  KEY idx_warehouse_city (city),
  KEY idx_warehouse_status (status),
  CONSTRAINT chk_warehouse_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_inventory_product_warehouse (product_id, warehouse_id),
  KEY idx_inventory_warehouse_id (warehouse_id),
  KEY idx_inventory_quantity (quantity),
  CONSTRAINT fk_inventory_product
    FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_inventory_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
  CONSTRAINT chk_inventory_quantity_non_negative CHECK (quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock_in (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stock_in_no VARCHAR(50) NOT NULL,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  operator_id BIGINT NOT NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stock_in_no (stock_in_no),
  KEY idx_stock_in_product_id (product_id),
  KEY idx_stock_in_warehouse_id (warehouse_id),
  KEY idx_stock_in_created_at (created_at),
  CONSTRAINT fk_stock_in_product
    FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_stock_in_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
  CONSTRAINT fk_stock_in_operator
    FOREIGN KEY (operator_id) REFERENCES app_user(id),
  CONSTRAINT chk_stock_in_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock_out (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stock_out_no VARCHAR(50) NOT NULL,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  operator_id BIGINT NOT NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stock_out_no (stock_out_no),
  KEY idx_stock_out_product_id (product_id),
  KEY idx_stock_out_warehouse_id (warehouse_id),
  KEY idx_stock_out_created_at (created_at),
  CONSTRAINT fk_stock_out_product
    FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_stock_out_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
  CONSTRAINT fk_stock_out_operator
    FOREIGN KEY (operator_id) REFERENCES app_user(id),
  CONSTRAINT chk_stock_out_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  change_type VARCHAR(30) NOT NULL,
  change_quantity INT NOT NULL,
  before_quantity INT NOT NULL,
  after_quantity INT NOT NULL,
  source_no VARCHAR(50) NOT NULL,
  operator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_inventory_flow_product_id (product_id),
  KEY idx_inventory_flow_warehouse_id (warehouse_id),
  KEY idx_inventory_flow_change_type (change_type),
  KEY idx_inventory_flow_created_at (created_at),
  CONSTRAINT fk_inventory_flow_product
    FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_inventory_flow_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
  CONSTRAINT fk_inventory_flow_operator
    FOREIGN KEY (operator_id) REFERENCES app_user(id),
  CONSTRAINT chk_inventory_flow_change_type
    CHECK (change_type IN ('STOCK_IN', 'STOCK_OUT', 'REDIS_DEDUCT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nl_query_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question VARCHAR(500) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  generated_sql TEXT NULL,
  checked_sql TEXT NULL,
  check_status VARCHAR(30) NOT NULL,
  result_summary TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_nl_query_log_user_id (user_id),
  KEY idx_nl_query_log_status (check_status),
  KEY idx_nl_query_log_created_at (created_at),
  CONSTRAINT fk_nl_query_log_user
    FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT chk_nl_query_log_status CHECK (check_status IN ('PASSED', 'REJECTED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS redis_deduct_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  request_id VARCHAR(100) NOT NULL,
  deduct_quantity INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  fail_reason VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_redis_deduct_request_id (request_id),
  KEY idx_redis_deduct_product_id (product_id),
  KEY idx_redis_deduct_warehouse_id (warehouse_id),
  KEY idx_redis_deduct_status (status),
  KEY idx_redis_deduct_created_at (created_at),
  CONSTRAINT fk_redis_deduct_product
    FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_redis_deduct_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
  CONSTRAINT chk_redis_deduct_quantity_positive CHECK (deduct_quantity > 0),
  CONSTRAINT chk_redis_deduct_status CHECK (status IN ('SUCCESS', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO app_user (username, password_hash, nickname, role, status)
VALUES
  ('admin', '{noop}123456', '系统管理员', 'ADMIN', 1),
  ('warehouse01', '{noop}123456', '仓库管理员01', 'WAREHOUSE_ADMIN', 1)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status);

INSERT INTO warehouse (warehouse_code, warehouse_name, city, status)
VALUES
  ('BJ001', '北京仓', '北京', 1),
  ('SH001', '上海仓', '上海', 1),
  ('GZ001', '广州仓', '广州', 1)
ON DUPLICATE KEY UPDATE
  warehouse_name = VALUES(warehouse_name),
  city = VALUES(city),
  status = VALUES(status);

INSERT INTO product (product_code, product_name, category, unit, safe_stock, status)
VALUES
  ('P001', '埃塞俄比亚咖啡豆', '咖啡豆', '袋', 20, 1),
  ('P002', '哥伦比亚咖啡豆', '咖啡豆', '袋', 20, 1),
  ('P003', '意式拼配咖啡豆', '咖啡豆', '袋', 30, 1),
  ('P004', '手冲滤纸', '耗材', '盒', 50, 1),
  ('P005', '拿铁杯', '杯具', '个', 40, 1),
  ('P006', '半自动咖啡机', '设备', '台', 5, 1),
  ('P007', '磨豆机', '设备', '台', 5, 1),
  ('P008', '咖啡外带杯', '耗材', '箱', 60, 1)
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  category = VALUES(category),
  unit = VALUES(unit),
  safe_stock = VALUES(safe_stock),
  status = VALUES(status);

INSERT INTO product_image (product_id, image_url, is_main, sort_order)
SELECT p.id, CONCAT('/uploads/products/', p.product_code, '-main.png'), 1, 1
FROM product p
WHERE NOT EXISTS (
  SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.is_main = 1
);

INSERT INTO product_image (product_id, image_url, is_main, sort_order)
SELECT p.id, CONCAT('/uploads/products/', p.product_code, '-detail-1.png'), 0, 2
FROM product p
WHERE NOT EXISTS (
  SELECT 1 FROM product_image pi
  WHERE pi.product_id = p.id AND pi.image_url = CONCAT('/uploads/products/', p.product_code, '-detail-1.png')
);

INSERT INTO inventory (product_id, warehouse_id, quantity)
SELECT p.id, w.id,
  CASE
    WHEN p.product_code IN ('P006', 'P007') THEN 8
    WHEN w.warehouse_code = 'BJ001' THEN 120
    WHEN w.warehouse_code = 'SH001' THEN 95
    ELSE 70
  END AS quantity
FROM product p
CROSS JOIN warehouse w
WHERE 1 = 1
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity);

INSERT INTO stock_in (stock_in_no, product_id, warehouse_id, quantity, operator_id, remark, created_at)
SELECT CONCAT('IN-DEMO-', p.product_code, '-', w.warehouse_code),
       p.id,
       w.id,
       i.quantity,
       u.id,
       '初始化入库',
       DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM inventory i
JOIN product p ON i.product_id = p.id
JOIN warehouse w ON i.warehouse_id = w.id
JOIN app_user u ON u.username = 'admin'
WHERE 1 = 1
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  remark = VALUES(remark);

INSERT INTO stock_out (stock_out_no, product_id, warehouse_id, quantity, operator_id, remark, created_at)
SELECT 'OUT-DEMO-P001-BJ-001', p.id, w.id, 18, u.id, '演示出库', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM product p JOIN warehouse w JOIN app_user u
WHERE p.product_code = 'P001' AND w.warehouse_code = 'BJ001' AND u.username = 'warehouse01'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO stock_out (stock_out_no, product_id, warehouse_id, quantity, operator_id, remark, created_at)
SELECT 'OUT-DEMO-P002-BJ-001', p.id, w.id, 12, u.id, '演示出库', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM product p JOIN warehouse w JOIN app_user u
WHERE p.product_code = 'P002' AND w.warehouse_code = 'BJ001' AND u.username = 'warehouse01'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO stock_out (stock_out_no, product_id, warehouse_id, quantity, operator_id, remark, created_at)
SELECT 'OUT-DEMO-P003-SH-001', p.id, w.id, 25, u.id, '演示出库', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM product p JOIN warehouse w JOIN app_user u
WHERE p.product_code = 'P003' AND w.warehouse_code = 'SH001' AND u.username = 'warehouse01'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO inventory_flow (product_id, warehouse_id, change_type, change_quantity, before_quantity, after_quantity, source_no, operator_id, created_at)
SELECT si.product_id, si.warehouse_id, 'STOCK_IN', si.quantity, 0, si.quantity, si.stock_in_no, si.operator_id, si.created_at
FROM stock_in si
WHERE NOT EXISTS (
  SELECT 1 FROM inventory_flow f WHERE f.source_no = si.stock_in_no
);

INSERT INTO inventory_flow (product_id, warehouse_id, change_type, change_quantity, before_quantity, after_quantity, source_no, operator_id, created_at)
SELECT so.product_id, so.warehouse_id, 'STOCK_OUT', so.quantity, i.quantity + so.quantity, i.quantity, so.stock_out_no, so.operator_id, so.created_at
FROM stock_out so
JOIN inventory i ON i.product_id = so.product_id AND i.warehouse_id = so.warehouse_id
WHERE NOT EXISTS (
  SELECT 1 FROM inventory_flow f WHERE f.source_no = so.stock_out_no
);
