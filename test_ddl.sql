CREATE USER 'ban'@'%' IDENTIFIED BY 'openapi';
GRANT ALL PRIVILEGES ON *.* TO 'ban'@'%';

CREATE DATABASE api;

USE oauth;

CREATE TABLE `portal_manage_of_agree` (
`id` varchar(20) NOT NULL,
`created_by` varchar(50),
`created_date` datetime,
`last_modified_by` varchar(50),
`last_modified_date` datetime,
`agree_id` varchar(20),
`agree_ver` varchar(20),
`body` longtext,
`del_yn` varchar(1),
`provider_code` varchar(20),
`provider_name` varchar(255),
`title` varchar(256),
`use_yn` varchar(1),
`agree_gb` varchar(20),
`highlight` longtext,
`htmlbody` longtext,
PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `oauth_code` (
  `code` VARCHAR(255) NOT NULL,
  `authentication` BLOB NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `oauth_users` (
  `ci` varchar(100) NOT NULL COMMENT 'CI',
  `uuid` varchar(100) NOT NULL COMMENT 'UUID',
  `affiliate_code` varchar(100) NOT NULL COMMENT '관계사 코드',
  `reg_date` datetime DEFAULT NULL COMMENT '등록 일자',
  `mod_date` datetime DEFAULT NULL COMMENT '수정 일자',
  PRIMARY KEY (`ci`,`uuid`,`affiliate_code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

CREATE TABLE `authorities` (
  `username` varchar(50) NOT NULL,
  `authority` varchar(50) NOT NULL,
  PRIMARY KEY (`username`,`authority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table oauth_access_token (
 `access_token_id` bigint(20) NOT NULL AUTO_INCREMENT,
 `authentication_id` varchar(256),
 `token_id` varchar(256),
 `token_value` varchar(1000),
 `user_name` varchar(256),
 `client_id` varchar(256),
 `refresh_token` varchar(256),
 `refresh_token_value` varchar(1000),
 `expire_date` datetime DEFAULT NULL COMMENT '토큰 만료 일자.',
 `reg_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '토큰 등록 일자.',
 `revoked` tinyint(1) DEFAULT 0 COMMENT 'revoke 여부. 사용 가능=0, 불가능=1',
 `revoke_date` datetime DEFAULT NULL COMMENT 'revoke된 일자',
 `refreshed` tinyint(1) DEFAULT 0 COMMENT 'refresh된 토큰 여부. 처음 생성=0, refresh된 토큰=1',
  PRIMARY KEY (`access_token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `oauth_refresh_token` (
  `refresh_token_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token_id` varchar(256) DEFAULT NULL,
  `token_value` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `expire_date` datetime DEFAULT NULL COMMENT '토큰 만료 일자.',
  `reg_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '토큰 등록 일자.',
  `revoked` tinyint(1) DEFAULT 0 COMMENT 'revoke 여부. 사용 가능=0, 불가능=1',
  `revoke_date` datetime DEFAULT NULL COMMENT 'revoke된 일자',
  PRIMARY KEY (`refresh_token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
