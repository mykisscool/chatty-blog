# Additional seeded comments to better demonstrate nested threads

# --- !Ups

INSERT INTO `comment` (`id`, `post_id`, `user_id`, `comment_id`, `comment`) VALUES
(14, 6, 8, 12, 'Yes.'),
(15, 6, 7, 12, 'No.'),
(16, 6, 2, 11, 'I see what you did there ... BUT ... the title is "PunchOut!!", not "DodgeOut!!"!'),
(17, 6, 3, 16, 'Whatever');

# --- !Downs

DELETE FROM `comment` WHERE `id` = 14;
DELETE FROM `comment` WHERE `id` = 15;
DELETE FROM `comment` WHERE `id` = 16;
DELETE FROM `comment` WHERE `id` = 17;
