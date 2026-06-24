-- =====================================================================
-- 線上投票系統 - 資料庫 DDL
-- 資料庫: MySQL / MariaDB
-- =====================================================================

DROP DATABASE IF EXISTS vote_system;
CREATE DATABASE vote_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vote_system;

-- ---------------------------------------------------------------------
-- 1. 投票項目表 (voting_item)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS voting_record;
DROP TABLE IF EXISTS voting_item;

CREATE TABLE voting_item (
    item_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '投票項目編號',
    item_name   VARCHAR(100)    NOT NULL                  COMMENT '投票項目名稱',
    vote_count  INT UNSIGNED    NOT NULL DEFAULT 0         COMMENT '累積票數(快取，可由觸發器或交易維護)',
    is_deleted  TINYINT(1)      NOT NULL DEFAULT 0         COMMENT '軟刪除標記 0=正常 1=已刪除',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票項目表';

-- ---------------------------------------------------------------------
-- 2. 投票紀錄表 (voting_record)
-- ---------------------------------------------------------------------
CREATE TABLE voting_record (
    record_id   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '投票紀錄編號',
    voter_name  VARCHAR(50)     NOT NULL                  COMMENT '投票人',
    item_id     BIGINT UNSIGNED NOT NULL                  COMMENT '投票項目編號 (FK)',
    voted_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投票時間',
    CONSTRAINT fk_voting_record_item
        FOREIGN KEY (item_id) REFERENCES voting_item (item_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_voting_record_item_id (item_id),
    INDEX idx_voting_record_voter_name (voter_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票紀錄表';


-- =====================================================================
-- Stored Procedures
-- 所有資料庫存取（CRUD + 投票）皆透過 Stored Procedure 進行，
-- 並使用參數化輸入以防止 SQL Injection。
-- =====================================================================

DELIMITER $$

-- ---------------------------------------------------------------------
-- sp_item_list：查詢所有投票項目(含目前票數)，未刪除者
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_item_list $$
CREATE PROCEDURE sp_item_list()
BEGIN
    SELECT item_id, item_name, vote_count, created_at, updated_at
    FROM voting_item
    WHERE is_deleted = 0
    ORDER BY item_id ASC;
END $$

-- ---------------------------------------------------------------------
-- sp_item_get：依編號查詢單一投票項目
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_item_get $$
CREATE PROCEDURE sp_item_get(
    IN p_item_id BIGINT UNSIGNED
)
BEGIN
    SELECT item_id, item_name, vote_count, created_at, updated_at
    FROM voting_item
    WHERE item_id = p_item_id
      AND is_deleted = 0;
END $$

-- ---------------------------------------------------------------------
-- sp_item_create：新增投票項目
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_item_create $$
CREATE PROCEDURE sp_item_create(
    IN  p_item_name VARCHAR(100),
    OUT p_new_id    BIGINT UNSIGNED
)
BEGIN
    INSERT INTO voting_item (item_name, vote_count)
    VALUES (p_item_name, 0);

    SET p_new_id = LAST_INSERT_ID();
END $$

-- ---------------------------------------------------------------------
-- sp_item_update：更新投票項目名稱
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_item_update $$
CREATE PROCEDURE sp_item_update(
    IN p_item_id   BIGINT UNSIGNED,
    IN p_item_name VARCHAR(100)
)
BEGIN
    UPDATE voting_item
    SET item_name = p_item_name
    WHERE item_id = p_item_id
      AND is_deleted = 0;
END $$

-- ---------------------------------------------------------------------
-- sp_item_delete：軟刪除投票項目
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_item_delete $$
CREATE PROCEDURE sp_item_delete(
    IN p_item_id BIGINT UNSIGNED
)
BEGIN
    UPDATE voting_item
    SET is_deleted = 1
    WHERE item_id = p_item_id;
END $$

-- ---------------------------------------------------------------------
-- sp_vote_cast：執行投票（寫入紀錄 + 累加票數），單一項目原子操作
-- 供 Service 層在 @Transactional 中對每個選擇的項目呼叫一次，
-- 多選時由應用層的 Transaction 包覆多次呼叫，確保全部成功或全部失敗。
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_vote_cast $$
CREATE PROCEDURE sp_vote_cast(
    IN p_voter_name VARCHAR(50),
    IN p_item_id    BIGINT UNSIGNED
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;

    SELECT COUNT(1) INTO v_exists
    FROM voting_item
    WHERE item_id = p_item_id
      AND is_deleted = 0;

    IF v_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '投票項目不存在或已刪除';
    END IF;

    INSERT INTO voting_record (voter_name, item_id)
    VALUES (p_voter_name, p_item_id);

    UPDATE voting_item
    SET vote_count = vote_count + 1
    WHERE item_id = p_item_id;
END $$

-- ---------------------------------------------------------------------
-- sp_record_list：查詢所有投票紀錄(可選擇依項目編號篩選)
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_record_list $$
CREATE PROCEDURE sp_record_list(
    IN p_item_id BIGINT UNSIGNED
)
BEGIN
    SELECT r.record_id, r.voter_name, r.item_id, i.item_name, r.voted_at
    FROM voting_record r
    JOIN voting_item i ON i.item_id = r.item_id
    WHERE p_item_id IS NULL OR r.item_id = p_item_id
    ORDER BY r.voted_at DESC;
END $$

DELIMITER ;
