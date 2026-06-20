CREATE TABLE IF NOT EXISTS user_profile (
    id     BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name   VARCHAR(50)  NOT NULL        COMMENT '姓名',
    avatar VARCHAR(255) NULL            COMMENT '头像URL',
    bio    TEXT         NULL            COMMENT '个人简介'
) COMMENT '个人信息';

CREATE TABLE IF NOT EXISTS social_link (
    id       BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    platform VARCHAR(50) NOT NULL       COMMENT '平台名称',
    url      VARCHAR(255) NOT NULL      COMMENT '链接地址',
    icon     VARCHAR(255) NULL          COMMENT '图标标识'
) COMMENT '社交链接';

CREATE TABLE IF NOT EXISTS post (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    title      VARCHAR(200) NOT NULL       COMMENT '标题',
    content    TEXT         NULL           COMMENT '正文',
    type       VARCHAR(20)  NOT NULL DEFAULT 'formal' COMMENT '类型：formal-正规推文 random-碎碎念',
    pinned     TINYINT(1)   NOT NULL DEFAULT 0  COMMENT '是否置顶',
    deleted    TINYINT(1)   NOT NULL DEFAULT 0  COMMENT '是否删除',
    created_at DATETIME     NOT NULL       COMMENT '创建时间',
    updated_at DATETIME     NOT NULL       COMMENT '修改时间'
) COMMENT '帖子';

CREATE TABLE IF NOT EXISTS anime (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name        VARCHAR(100) NOT NULL       COMMENT '番名',
    cover_url   VARCHAR(255) NULL           COMMENT '封面图URL',
    description TEXT         NULL           COMMENT '简介',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
    created_at  DATETIME     NOT NULL       COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL       COMMENT '修改时间'
) COMMENT '番剧';

CREATE TABLE IF NOT EXISTS anime_review (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    anime_id     BIGINT       NOT NULL       COMMENT '关联番剧ID',
    content      TEXT         NOT NULL       COMMENT '评价内容',
    created_time DATETIME     NOT NULL       COMMENT '评价时间',
    deleted      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '番剧评价';
