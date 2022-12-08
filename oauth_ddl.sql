CREATE USER 'ban'@'%' IDENTIFIED BY 'openapi';
GRANT ALL PRIVILEGES ON *.* TO 'ban'@'%';

CREATE DATABASE oauth;

USE oauth;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(50) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(5) DEFAULT 'true',
  `expire_date` varchar(10) DEFAULT NULL,
  `org_code` varchar(256) DEFAULT NULL,
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
