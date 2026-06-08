USE inventory_mvp;

ALTER TABLE app_user
  ADD COLUMN avatar_url VARCHAR(255) NULL AFTER nickname;
