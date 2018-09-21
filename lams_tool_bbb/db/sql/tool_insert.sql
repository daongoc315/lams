
 
INSERT INTO lams_tool
(
tool_signature,
service_name,
tool_display_name,
description,
tool_identifier,
tool_version,
valid_flag,
grouping_support_type_id,
learner_url,
learner_preview_url,
learner_progress_url,
author_url,
monitor_url,
help_url,
admin_url,
language_file,
create_date_time,
modified_date_time
)
VALUES
(
'labbb10',
'bbbService',
'Bbb',
'Bbb',
'bbb',
'@tool_version@',
0,
2,
'tool/labbb10/learning.do?mode=learner',
'tool/labbb10/learning.do?mode=author',
'tool/labbb10/learning.do?mode=teacher',
'tool/labbb10/authoring.do',
'tool/labbb10/monitoring.do',
NULL,
'tool/labbb10/admin/view.do',
'org.lamsfoundation.lams.tool.bbb.ApplicationResources',
NOW(),
NOW()
)
