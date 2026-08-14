-- 实验室设备借还报修系统初始化脚本
-- 包含：建库、建表以及可直接演示借用、审批、归还、报修流程的基础数据
CREATE DATABASE IF NOT EXISTS lab_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_equipment;

-- 为了便于重复执行脚本，先按外键依赖的逆序删除旧表
DROP TABLE IF EXISTS system_log;
DROP TABLE IF EXISTS notification_message;
DROP TABLE IF EXISTS approval_record;
DROP TABLE IF EXISTS repair_ticket;
DROP TABLE IF EXISTS borrow_request;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS app_user;

-- 用户表：保存学生、实验室管理员、指导教师和院领导等角色
CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 设备表：记录实验室设备的基本信息、类别、状态和保管位置
CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    lab_room VARCHAR(100) NOT NULL,
    purchase_value DECIMAL(12, 2) NOT NULL,
    manager VARCHAR(50),
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 借用申请表：记录借用时间、审批角色、归还时间和逾期费用
CREATE TABLE borrow_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    approver_id BIGINT NULL,
    start_date DATE NOT NULL,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE NULL,
    purpose VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    required_approver_role VARCHAR(30) NOT NULL,
    reject_reason VARCHAR(500),
    overdue_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_borrow_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_borrow_applicant FOREIGN KEY (applicant_id) REFERENCES app_user(id),
    CONSTRAINT fk_borrow_approver FOREIGN KEY (approver_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 报修工单表：记录故障描述、处理人和维修结果
CREATE TABLE repair_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    handler_id BIGINT NULL,
    fault_description VARCHAR(500) NOT NULL,
    repair_result VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    CONSTRAINT fk_repair_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_repair_reporter FOREIGN KEY (reporter_id) REFERENCES app_user(id),
    CONSTRAINT fk_repair_handler FOREIGN KEY (handler_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审批记录表：保留借用申请每次流转时的操作痕迹
CREATE TABLE approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    borrow_request_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    comment VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_record_borrow FOREIGN KEY (borrow_request_id) REFERENCES borrow_request(id),
    CONSTRAINT fk_record_operator FOREIGN KEY (operator_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 通知表：配合观察者模式向用户发送站内通知
CREATE TABLE notification_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(800) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统日志表：记录借用、归还、报修、报废等关键业务事件
CREATE TABLE system_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化基础账号，覆盖借用申请和审批链中的核心角色
INSERT INTO app_user(username, real_name, role, department) VALUES
('student01', '欧书团', 'STUDENT', '软件工程2023级'),
('labadmin', '王老师', 'LAB_ADMIN', '人工智能实验室'),
('teacher', '李教授', 'TEACHER', '计算机与数学学院'),
('dean', '张院长', 'DEAN', '计算机与数学学院');

-- 初始化基础设备数据，便于直接演示不同类别设备的审批与费用策略
INSERT INTO equipment(code, name, category, status, lab_room, purchase_value, manager, description, created_at, updated_at) VALUES
('LAB-N-001', '数字万用表', 'NORMAL', 'AVAILABLE', '综合实验室A301', 680.00, '王老师', '常规电路实验测量设备', NOW(), NOW()),
('LAB-C-002', '深度学习工作站', 'COMPUTER', 'AVAILABLE', '人工智能实验室B204', 15800.00, '李教授', 'GPU计算与课程设计实验设备', NOW(), NOW()),
('LAB-P-003', '高精度示波器', 'PRECISION', 'AVAILABLE', '电子技术实验室C102', 42600.00, '张院长', '高价值精密仪器，需高级审批', NOW(), NOW()),
('LAB-N-004', 'Arduino 传感器套件', 'NORMAL', 'AVAILABLE', '创新实验室D201', 360.00, '王老师', '创新训练和课程实验套件', NOW(), NOW());
