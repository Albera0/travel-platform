DROP DATABASE IF EXISTS travel_platform;
CREATE DATABASE travel_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE travel_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- USER
-- =========================
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
                         id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                         phone VARCHAR(11) NOT NULL UNIQUE,
                         password VARCHAR(128) DEFAULT '',
                         nick_name VARCHAR(32) DEFAULT '',
                         icon VARCHAR(255) DEFAULT '',
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS tb_user_info;
CREATE TABLE tb_user_info (
                              user_id BIGINT UNSIGNED PRIMARY KEY,
                              city VARCHAR(64) DEFAULT '',
                              introduce VARCHAR(255) DEFAULT '',
                              fans INT UNSIGNED DEFAULT 0,
                              followee INT UNSIGNED DEFAULT 0,
                              gender TINYINT UNSIGNED DEFAULT 0,
                              birthday DATE,
                              credits INT UNSIGNED DEFAULT 0,
                              level TINYINT UNSIGNED DEFAULT 0,
                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS tb_follow;
CREATE TABLE tb_follow (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT UNSIGNED NOT NULL,
                           follow_user_id BIGINT UNSIGNED NOT NULL,
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- DESTINATION
-- =========================
DROP TABLE IF EXISTS tb_destination_type;
CREATE TABLE tb_destination_type (
                                     id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(32),
                                     icon VARCHAR(255),
                                     sort INT DEFAULT 0,
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS tb_destination;
CREATE TABLE tb_destination (
                                id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                                name VARCHAR(128) NOT NULL,
                                type_id BIGINT UNSIGNED NOT NULL,
                                images VARCHAR(2048) NOT NULL,
                                area VARCHAR(128),
                                address VARCHAR(255),
                                x DOUBLE,
                                y DOUBLE,
                                avg_price BIGINT DEFAULT 0,
                                sold INT DEFAULT 0,
                                comments INT DEFAULT 0,
                                score INT DEFAULT 0,
                                open_hours VARCHAR(64),
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- POSTS
-- =========================
DROP TABLE IF EXISTS tb_travel_post;

CREATE TABLE tb_travel_post (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

                                destination_id BIGINT COMMENT '景点id',
                                user_id BIGINT COMMENT '用户id',


                                user_name VARCHAR(50) COMMENT '用户姓名（冗余）',
                                user_icon VARCHAR(255) COMMENT '用户头像（冗余）',

                                title VARCHAR(255) COMMENT '标题',
                                images VARCHAR(2048) COMMENT '图片',
                                content TEXT COMMENT '内容',

                                liked INT DEFAULT 0 COMMENT '点赞数量',
                                comments INT DEFAULT 0 COMMENT '评论数量',

                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- PACKAGE / COUPON
-- =========================
DROP TABLE IF EXISTS tb_travel_package;

CREATE TABLE tb_travel_package (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                   destination_id BIGINT COMMENT '景点id',
                                   title VARCHAR(255) COMMENT '门票标题',
                                   sub_title VARCHAR(255) COMMENT '副标题',
                                   rules VARCHAR(1024) COMMENT '使用规则',
                                   pay_value BIGINT COMMENT '支付金额',
                                   actual_value BIGINT COMMENT '抵扣金额',
                                   type TINYINT DEFAULT 0 COMMENT '门票类型',
                                   status TINYINT DEFAULT 1 COMMENT '门票状态',
                                   stock INT DEFAULT 0 COMMENT '库存',
                                   begin_time DATETIME COMMENT '生效时间',
                                   end_time DATETIME COMMENT '失效时间',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS tb_seckill_package;
CREATE TABLE tb_seckill_package (
                                    package_id BIGINT PRIMARY KEY,
                                    stock INT,
                                    begin_time TIMESTAMP,
                                    end_time TIMESTAMP,
                                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS tb_travel_order;
CREATE TABLE tb_travel_order (
                                 id BIGINT PRIMARY KEY,
                                 user_id BIGINT,
                                 package_id BIGINT,
                                 pay_type TINYINT DEFAULT 1,
                                 status TINYINT DEFAULT 1,
                                 travel_date DATE,
                                 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 pay_time TIMESTAMP NULL,
                                 use_time TIMESTAMP NULL,
                                 refund_time TIMESTAMP NULL,
                                 update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- AI
-- =========================
DROP TABLE IF EXISTS tb_agent_session;
CREATE TABLE tb_agent_session (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  user_id BIGINT,
                                  session_title VARCHAR(255),
                                  status TINYINT DEFAULT 1,
                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS tb_travel_task;
CREATE TABLE tb_travel_task (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                session_id BIGINT,
                                task_no VARCHAR(64),
                                destination VARCHAR(128),
                                days INT,
                                budget BIGINT,
                                user_prompt TEXT,
                                status TINYINT DEFAULT 1,
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS tb_travel_plan;
CREATE TABLE tb_travel_plan (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                task_id BIGINT,
                                version INT DEFAULT 1,
                                estimated_cost BIGINT,
                                ai_model VARCHAR(64),
                                is_current TINYINT DEFAULT 1,
                                plan_content TEXT,
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- MOCK DATA
-- =========================
INSERT INTO tb_user(id,phone,password,nick_name) VALUES
                                                     (1,'13800000001','123','Alex'),
                                                     (2,'13800000002','123','Bob'),
                                                     (3,'13800000003','123','Cindy');

INSERT INTO tb_destination_type(id,name) VALUES
                                             (1,'主题乐园'),(2,'历史文化'),(3,'自然风景'),(4,'城市地标');

INSERT INTO tb_destination(name,type_id,images,area,address,x,y,avg_price,sold,comments,score,open_hours) VALUES
                                                                                                              ('北京故宫',2,'img1','北京','东城区',116.39,39.90,60,12000,5000,49,'08:30-17:00'),
                                                                                                              ('上海迪士尼',1,'img2','上海','浦东',121.66,31.14,719,15000,8000,49,'08:30-21:30'),
                                                                                                              ('广州塔',4,'img3','广州','海珠区',113.33,23.11,150,9000,4100,48,'09:00-22:00'),
                                                                                                              ('西湖',3,'img4','杭州','西湖区',120.15,30.25,0,20000,9000,50,'全天');

INSERT INTO tb_travel_post
(destination_id, user_id, user_name, user_icon, title, images, content, liked, comments, create_time, update_time)
VALUES
    (1, 1, '小明', 'icon1.png',
     '故宫深度一日游攻略',
     'img1.jpg,img2.jpg,img3.jpg',
     '建议早上8点前入园，避开人流，重点看太和殿和珍宝馆。',
     120, 35,
     '2026-05-10 09:00:00', '2026-05-10 09:00:00'),

    (1, 2, '小红', 'icon2.png',
     '故宫拍照最佳路线',
     'img4.jpg,img5.jpg,img6.jpg',
     '午门进入→太和殿→御花园，下午光线适合拍照。',
     98, 22,
     '2026-05-10 10:30:00', '2026-05-10 10:30:00'),

    (2, 3, '小李', 'icon3.png',
     '迪士尼省时通关攻略',
     'img7.jpg,img8.jpg,img9.jpg',
     '建议提前买快速通行证，优先飞跃地平线。',
     310, 60,
     '2026-05-11 08:20:00', '2026-05-11 08:20:00'),

    (2, 4, '小王', 'icon4.png',
     '迪士尼亲子游体验分享',
     'img10.jpg,img11.jpg',
     '适合带小孩，幻想世界和小熊维尼最推荐。',
     180, 41,
     '2026-05-11 09:15:00', '2026-05-11 09:15:00'),

    (3, 1, '小明', 'icon1.png',
     '上海外滩夜景攻略',
     'img12.jpg,img13.jpg,img14.jpg',
     '晚上8点灯光最美，从外白渡桥开始走最好。',
     250, 55,
     '2026-05-12 19:00:00', '2026-05-12 19:00:00'),

    (3, 2, '小红', 'icon2.png',
     '外滩拍照机位推荐',
     'img15.jpg,img16.jpg',
     '和平饭店对面是最佳机位，可以拍陆家嘴全景。',
     210, 48,
     '2026-05-12 20:10:00', '2026-05-12 20:10:00'),

    (4, 3, '小李', 'icon3.png',
     '西湖一日游路线规划',
     'img17.jpg,img18.jpg,img19.jpg',
     '断桥→白堤→苏堤→雷峰塔，一天刚好走完。',
     330, 72,
     '2026-05-13 08:00:00', '2026-05-13 08:00:00'),

    (4, 4, '小王', 'icon4.png',
     '西湖骑行体验分享',
     'img20.jpg,img21.jpg',
     '租共享单车环湖非常舒服，春秋最佳。',
     190, 33,
     '2026-05-13 09:30:00', '2026-05-13 09:30:00');
-- COUPONS / PACKAGES

INSERT INTO tb_travel_package(
    id,destination_id,title,sub_title,rules,
    pay_value,actual_value,type,status,
    stock,begin_time,end_time,
    create_time,update_time
) VALUES
      (1,2,'上海迪士尼门票','双人票','不可退',
       99900,129900,0,1,
       200,'2026-05-01 00:00:00','2026-12-31 23:59:59',
       NOW(),NOW()),

      (2,1,'北京故宫讲解','含导游','不可退',
       19900,29900,1,1,
       80,'2026-05-01 00:00:00','2026-10-01 23:59:59',
       NOW(),NOW()),

      (3,4,'西湖一日游','含船票','可退',
       9900,19900,0,1,
       150,'2026-05-01 00:00:00','2026-09-30 23:59:59',
       NOW(),NOW()),

      (4,3,'广州塔夜游','观景','限时',
       8900,15900,1,1,
       120,'2026-05-01 18:00:00','2026-12-31 23:59:59',
       NOW(),NOW());

INSERT INTO tb_seckill_package(package_id,stock,begin_time,end_time) VALUES
                                                                         (2,200,NOW(),DATE_ADD(NOW(),INTERVAL 7 DAY)),
                                                                         (4,100,NOW(),DATE_ADD(NOW(),INTERVAL 5 DAY));

-- AI
INSERT INTO tb_agent_session(id,user_id,session_title) VALUES
    (1,1,'东京旅行规划');

INSERT INTO tb_travel_task(id,session_id,task_no,destination,days,budget,user_prompt,status) VALUES
    (1,1,'TASK_001','东京',5,3000,'东京5天预算3000',2);

INSERT INTO tb_travel_plan(task_id,version,estimated_cost,ai_model,is_current,plan_content) VALUES
    (1,1,2800,'gpt',1,'day1...');

SET FOREIGN_KEY_CHECKS=1;


DROP TABLE IF EXISTS tb_travel_post_comment;

CREATE TABLE tb_travel_post_comment (
                                        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',

                                        post_id BIGINT UNSIGNED NOT NULL COMMENT '攻略id（对应 travel_post.id）',

                                        user_id BIGINT UNSIGNED NOT NULL COMMENT '评论用户id',

                                        parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '一级评论id（0表示一级评论）',

                                        answer_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复的评论id（用于@回复）',

                                        content VARCHAR(500) NOT NULL COMMENT '评论内容',

                                        liked INT UNSIGNED DEFAULT 0 COMMENT '点赞数',

                                        status TINYINT UNSIGNED DEFAULT 0 COMMENT '状态：0正常 1举报 2屏蔽',

                                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                        PRIMARY KEY (id),

                                        KEY idx_post_id (post_id),
                                        KEY idx_user_id (user_id),
                                        KEY idx_parent_id (parent_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
