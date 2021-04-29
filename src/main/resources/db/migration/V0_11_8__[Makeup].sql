CREATE TABLE `auditor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime(6) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `command` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `command` text,
  `command_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `command_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `command` text,
  `command_type` varchar(255) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `log` text,
  `success` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `configuration_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6khurlrwucw93b2mff7rd979r` (`name`)
);


CREATE TABLE `connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `description` text,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `target` varchar(255) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `username` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_boj4rcgfevb82rpvflfekrdlc` (`name`)
);


CREATE TABLE `job_build` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime(6) DEFAULT NULL,
  `number` int(11) NOT NULL,
  `phase` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `job_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_phase_date` (`phase`,`date`),
  KEY `IDX_job_id_number_date` (`job_id`,`number`,`date`)
);


CREATE TABLE `privilege` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
);


CREATE TABLE `server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(20) DEFAULT NULL,
  `token` varchar(50) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qsgmrlgxfgo1v98jewa76i5j5` (`name`),
  UNIQUE KEY `UK_nku946e5qrmv5gudptbiuvgfv` (`url`)
);


CREATE TABLE `subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` text,
  `mandatory` bit(1) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `notified` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_p1jgir6qcpmqnxt4a8105wsot` (`name`)
);


CREATE TABLE `template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` text,
  `model` text,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mc6r7feujo1wdd6vw5esv4jba` (`name`)
);


CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `avatar` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `reset_code` varchar(255) DEFAULT NULL,
  `token_created_at` datetime(6) DEFAULT NULL,
  `username` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
);


CREATE TABLE `auditor_data` (
  `id` bigint(20) NOT NULL,
  `data` text,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`type`),
  CONSTRAINT `FKjqthxm7qjtc0ehnv76y4x3epf` FOREIGN KEY (`id`) REFERENCES `auditor` (`id`)
);


CREATE TABLE `configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `max_length` int(11) NOT NULL,
  `min_length` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parameter` varchar(255) DEFAULT NULL,
  `pattern` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rsbnorxj5j5imdk0o7p1tox8f` (`parameter`),
  KEY `FKihrobtjy9opuj6t56gb7n20xe` (`group_id`),
  CONSTRAINT `FKihrobtjy9opuj6t56gb7n20xe` FOREIGN KEY (`group_id`) REFERENCES `configuration_group` (`id`)
);


CREATE TABLE `job_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `failure_timestamp` datetime(6) DEFAULT NULL,
  `flow` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `build_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2yne0xnxnlqcklft9ffy4n8xd` (`build_id`),
  CONSTRAINT `FK2yne0xnxnlqcklft9ffy4n8xd` FOREIGN KEY (`build_id`) REFERENCES `job_build` (`id`)
);


CREATE TABLE `subject_channel` (
  `id` bigint(20) NOT NULL,
  `channel` varchar(255) DEFAULT NULL,
  KEY `FKd70f3pw0ef47odcbk3moqrpmx` (`id`),
  CONSTRAINT `FKd70f3pw0ef47odcbk3moqrpmx` FOREIGN KEY (`id`) REFERENCES `subject` (`id`)
);


CREATE TABLE `subject_swimlanes` (
  `id` bigint(20) NOT NULL,
  `criteria` varchar(255) DEFAULT NULL,
  `swimlane` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`swimlane`),
  CONSTRAINT `FKdta5wlt3h38p55bawnfc5quhv` FOREIGN KEY (`id`) REFERENCES `subject` (`id`)
);


CREATE TABLE `subject_user` (
  `subject_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  KEY `FK6xbhhnati8etb23t70b4cknnh` (`user_id`),
  KEY `FK2nwoey17n8mi4hyetyb6xvy8q` (`subject_id`),
  CONSTRAINT `FK2nwoey17n8mi4hyetyb6xvy8q` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FK6xbhhnati8etb23t70b4cknnh` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
);


CREATE TABLE `user_privilege` (
  `user_id` bigint(20) NOT NULL,
  `privilege_id` bigint(20) NOT NULL,
  KEY `FKmru0v5b2y73od0o5ejfe4m9v8` (`privilege_id`),
  KEY `FKbun0vye5x60x4jat4inmt6wbj` (`user_id`),
  CONSTRAINT `FKbun0vye5x60x4jat4inmt6wbj` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKmru0v5b2y73od0o5ejfe4m9v8` FOREIGN KEY (`privilege_id`) REFERENCES `privilege` (`id`)
);


CREATE TABLE `user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKa68196081fvovjhkek5m97n3y` (`role_id`),
  CONSTRAINT `FK859n2jvi8ivhui0rl0esws6o` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKa68196081fvovjhkek5m97n3y` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
);


CREATE TABLE `workbench_email` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `external_recipient` varchar(255) DEFAULT NULL,
  `query` text,
  `subject` varchar(255) NOT NULL,
  `connection_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs8rbntdla5gydcwjmjs14oq73` (`connection_id`),
  KEY `FKdmkgpclr7toccg4mtwi3g4lh3` (`user_id`),
  CONSTRAINT `FKdmkgpclr7toccg4mtwi3g4lh3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKs8rbntdla5gydcwjmjs14oq73` FOREIGN KEY (`connection_id`) REFERENCES `connection` (`id`)
);


CREATE TABLE `workbench_email_recipient` (
  `workbench_email_id` bigint(20) NOT NULL,
  `recipient` varchar(255) DEFAULT NULL,
  KEY `FKfohvibrp39toxvepyafr48y2v` (`workbench_email_id`),
  CONSTRAINT `FKfohvibrp39toxvepyafr48y2v` FOREIGN KEY (`workbench_email_id`) REFERENCES `workbench_email` (`id`)
);


CREATE TABLE `workbench_query` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `query` text,
  `shared` bit(1) NOT NULL,
  `connection_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK966c0nxn3h64xjfqugl76fwm` (`connection_id`),
  KEY `FKtplxvju1r5ugo3x9ltuuuvstg` (`user_id`),
  CONSTRAINT `FK966c0nxn3h64xjfqugl76fwm` FOREIGN KEY (`connection_id`) REFERENCES `connection` (`id`),
  CONSTRAINT `FKtplxvju1r5ugo3x9ltuuuvstg` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
);


CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `alias` varchar(255) DEFAULT NULL,
  `any_scope` bit(1) NOT NULL,
  `checkup_notified` bit(1) NOT NULL,
  `description` text,
  `enabled` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `notify` bit(1) NOT NULL,
  `rebuild` bit(1) NOT NULL,
  `rebuild_blocked` bit(1) NOT NULL,
  `retry` int(11) NOT NULL,
  `time_restriction` varchar(255) DEFAULT NULL,
  `tolerance` int(11) NOT NULL,
  `wait` int(11) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `server_id` bigint(20) DEFAULT NULL,
  `status_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_atcl7ldp04r846fq0cep4e3wi` (`name`),
  KEY `IDX_name` (`name`),
  KEY `FKihd6m3auwpenduntl3e1opcoq` (`user_id`),
  KEY `FKrw0dhkouwo84p8lhhf18hjoac` (`server_id`),
  KEY `FKlkkyf8i87x44eiq9axpnj1h0b` (`status_id`),
  CONSTRAINT `FKihd6m3auwpenduntl3e1opcoq` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKlkkyf8i87x44eiq9axpnj1h0b` FOREIGN KEY (`status_id`) REFERENCES `job_status` (`id`),
  CONSTRAINT `FKrw0dhkouwo84p8lhhf18hjoac` FOREIGN KEY (`server_id`) REFERENCES `server` (`id`)
);


CREATE TABLE `job_approval` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approved` bit(1) NOT NULL,
  `date` datetime(6) DEFAULT NULL,
  `description` text,
  `job_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKawbp0wo5yjwmuqc8n5s57fh78` (`job_id`),
  KEY `FKsh3kv6yj92fabbi8woyavmgqg` (`user_id`),
  CONSTRAINT `FKawbp0wo5yjwmuqc8n5s57fh78` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `FKsh3kv6yj92fabbi8woyavmgqg` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
);


CREATE TABLE `job_channel` (
  `id` bigint(20) NOT NULL,
  `channel` varchar(255) DEFAULT NULL,
  KEY `FK1sxah4mr4amd4jyo52gqmfpn9` (`id`),
  CONSTRAINT `FK1sxah4mr4amd4jyo52gqmfpn9` FOREIGN KEY (`id`) REFERENCES `job` (`id`)
);


CREATE TABLE `job_checkup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `conditional` varchar(255) DEFAULT NULL,
  `description` text,
  `enabled` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `prevalidation` bit(1) NOT NULL,
  `query` text,
  `scope` varchar(255) DEFAULT NULL,
  `threshold` varchar(255) DEFAULT NULL,
  `connection_id` bigint(20) DEFAULT NULL,
  `job_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKef9n32wdakepm4xlpe41mbecq` (`connection_id`),
  KEY `FKni80i6gijf3jxqjs1xlpwbg5s` (`job_id`),
  CONSTRAINT `FKef9n32wdakepm4xlpe41mbecq` FOREIGN KEY (`connection_id`) REFERENCES `connection` (`id`),
  CONSTRAINT `FKni80i6gijf3jxqjs1xlpwbg5s` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
);


CREATE TABLE `job_checkup_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `conditional` varchar(255) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `query` text,
  `scope` int(11) DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `threshold` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `job_checkup_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1knteuurvf3rswwos1yqceqws` (`job_checkup_id`),
  CONSTRAINT `FK1knteuurvf3rswwos1yqceqws` FOREIGN KEY (`job_checkup_id`) REFERENCES `job_checkup` (`id`)
);


CREATE TABLE `job_checkup_log_command_log` (
  `job_checkup_log_id` bigint(20) NOT NULL,
  `command_log_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_nl5uryr6v22o35fc1ou6txycx` (`command_log_id`),
  KEY `FK51gqkmsuooadadjqsookfa8e9` (`job_checkup_log_id`),
  CONSTRAINT `FK51gqkmsuooadadjqsookfa8e9` FOREIGN KEY (`job_checkup_log_id`) REFERENCES `job_checkup_log` (`id`),
  CONSTRAINT `FK6cuaxtwwubm4bt9soh1iv41vk` FOREIGN KEY (`command_log_id`) REFERENCES `command_log` (`id`)
);


CREATE TABLE `job_checkup_trigger` (
  `job_checkup_id` bigint(20) NOT NULL,
  `job_id` bigint(20) NOT NULL,
  KEY `FK9u10ph6k4pulspwwktbhv7vgx` (`job_id`),
  KEY `FKdtufuwcfqgettl79i4qwa45fj` (`job_checkup_id`),
  CONSTRAINT `FK9u10ph6k4pulspwwktbhv7vgx` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `FKdtufuwcfqgettl79i4qwa45fj` FOREIGN KEY (`job_checkup_id`) REFERENCES `job_checkup` (`id`)
);


CREATE TABLE `job_parent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blocker` bit(1) NOT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `job_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcv4n5rkftf2dyr3s7aqymjq3a` (`job_id`),
  KEY `FK5c0ieulq4d53l14pfars3ngcu` (`parent_id`),
  CONSTRAINT `FK5c0ieulq4d53l14pfars3ngcu` FOREIGN KEY (`parent_id`) REFERENCES `job` (`id`),
  CONSTRAINT `FKcv4n5rkftf2dyr3s7aqymjq3a` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
);


CREATE TABLE `job_subject` (
  `job_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL,
  KEY `FKnnmrsn3kfddli5rtmll9rjp6j` (`subject_id`),
  KEY `FKl0pm43fo0uf8klhe3uljo3q7g` (`job_id`),
  CONSTRAINT `FKl0pm43fo0uf8klhe3uljo3q7g` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `FKnnmrsn3kfddli5rtmll9rjp6j` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`)
);


CREATE TABLE `job_workbench_email` (
  `job_id` bigint(20) NOT NULL,
  `workbench_email_id` bigint(20) NOT NULL,
  KEY `FKbvhnl6b2mw9riexm99qtidrde` (`workbench_email_id`),
  KEY `FKo5xlefvevusa7fr5mrgk9ehar` (`job_id`),
  CONSTRAINT `FKbvhnl6b2mw9riexm99qtidrde` FOREIGN KEY (`workbench_email_id`) REFERENCES `workbench_email` (`id`),
  CONSTRAINT `FKo5xlefvevusa7fr5mrgk9ehar` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
);


CREATE TABLE `checkup_command` (
  `job_checkup_id` bigint(20) NOT NULL,
  `command_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_i8gvangoyfb5adj0bbswxdn1o` (`command_id`),
  KEY `FKj006mp9fpq5io422wf07bbuqo` (`job_checkup_id`),
  CONSTRAINT `FKj006mp9fpq5io422wf07bbuqo` FOREIGN KEY (`job_checkup_id`) REFERENCES `job_checkup` (`id`),
  CONSTRAINT `FKsi326mo2mscav0slqlp5ysqm8` FOREIGN KEY (`command_id`) REFERENCES `command` (`id`)
);