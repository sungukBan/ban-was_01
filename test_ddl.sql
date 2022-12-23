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


CREATE TABLE `portal_manage_of_agree` (
`agree_id` varchar(20) NOT NULL,
`agree_ver` varchar(20) NOT NULL,
`body` longtext,
`sts` char(1),
`created_id` varchar(50),
`created_date` datetime,
PRIMARY KEY (`agree_id`, `agree_ver`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `personal_info_agree` (
`cino` varchar(88) NOT NULL,
`agree_id` varchar(20) NOT NULL,
`agree_ver` varchar(20) NOT NULL,
`agree_gb` varchar(20),
`agree_yn` char(1),
`appname` varchar(50),
`corpcd` varchar(50),
`corpname` varchar(50),
`app_key` varchar(50),
`agreedate` datetime,
PRIMARY KEY (`cino`, `agree_id`, `agree_ver`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tb_app` (
`id`                           varchar(36)  ,
`created_by`                   varchar(50)  ,
`created_date`                 timestamp    ,
`last_modified_by`             varchar(50)  ,
`last_modified_date`           timestamp    ,
`record_state`                 int(11)      ,
`app_key`                      varchar(255) ,
`correl_id`                    varchar(255) ,
`description`                  longtext     ,
`expiry_date`                  date         ,
`is_test_app`                  bit(1)       ,
`issuer`                       varchar(255) ,
`key_secret`                   varchar(255) ,
`name`                         varchar(255) ,
`o_auth_callback_url`          longtext     ,
`o_auth_scope`                 varchar(255) ,
`o_auth_type`                  varchar(255) ,
`platform`                     varchar(255) ,
`serial_number`                int(11)      ,
`status`                       varchar(255) ,
`version`                      int(11)      ,
`white_list`                   varchar(4000),
`creator_id`                   varchar(36)  ,
`image_file_id`                varchar(36)  ,
`organization_id`              varchar(36)  ,
`detail_status`                varchar(255) ,
`reserved_attribute1`          varchar(255) ,
`reserved_attribute10`         varchar(255) ,
`reserved_attribute2`          varchar(255) ,
`reserved_attribute3`          varchar(255) ,
`reserved_attribute4`          varchar(255) ,
`reserved_attribute5`          varchar(255) ,
`reserved_attribute6`          varchar(255) ,
`reserved_attribute7`          varchar(255) ,
`reserved_attribute8`          varchar(255) ,
`reserved_attribute9`          varchar(255) ,
`access_token_life_time`       varchar(255) ,
`refresh_token_life_time`      varchar(255) ,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tb_organization` (
`id`                      varchar(36) ,
`created_by`              varchar(50) ,
`created_date`            timestamp   ,
`last_modified_by`        varchar(50) ,
`last_modified_date`      timestamp   ,
`record_state`            int(11)     ,
`reserved_attribute1`     varchar(255),
`reserved_attribute10`    varchar(255),
`reserved_attribute2`     varchar(255),
`reserved_attribute3`     varchar(255),
`reserved_attribute4`     varchar(255),
`reserved_attribute5`     varchar(255),
`reserved_attribute6`     varchar(255),
`reserved_attribute7`     varchar(255),
`reserved_attribute8`     varchar(255),
`reserved_attribute9`     varchar(255),
`address`                 varchar(255),
`ceo_name`                varchar(255),
`code`                    varchar(16) ,
`corp_reg_number`         varchar(255),
`description`             varchar(255),
`gateway_domain_name`     varchar(128),
`homepage_url`            varchar(255),
`license_number`          varchar(12) ,
`main_phone`              varchar(255),
`name`                    varchar(50) ,
`sectors`                 varchar(255),
`state`                   varchar(30) ,
`type`                    varchar(255),
`version`                 int(11),
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;