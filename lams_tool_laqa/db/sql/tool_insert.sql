# Connection: ROOT LOCAL
# Host: localhost
# Saved: 2005-04-07 10:42:43
# 
INSERT INTO lams_tool
(
tool_signature,
service_name,
tool_display_name,
description,
tool_identifier,
tool_version,
learning_library_id,
default_tool_content_id,
valid_flag,
grouping_support_type_id,
supports_run_offline_flag,
learner_url,
learner_preview_url,
learner_progress_url,
author_url,
monitor_url,
define_later_url,
export_pfolio_learner_url,
export_pfolio_class_url,
contribute_url,
moderation_url,
language_file,
create_date_time
)
VALUES
(
'laqa11',
'qaService',
'Question and Answer',
'Question and Answer Tool Description',
'qa',
'1.1',
NULL,
NULL,
0,
2,
1,
'tool/laqa11/learningStarter.do?mode=learner',
'tool/laqa11/learningStarter.do?mode=author',
'tool/laqa11/learningStarter.do?mode=teacher',
'tool/laqa11/authoringStarter.do',
'tool/laqa11/monitoringStarter.do',
'tool/laqa11/defineLaterStarter.do',
'tool/laqa11/export.do?mode=learner',
'tool/laqa11/export.do?mode=teacher',
'tool/laqa11/monitoring.do',
'tool/laqa11/monitoring.do',
'org.lamsfoundation.lams.tool.qa.QaResources',
NOW()
)
