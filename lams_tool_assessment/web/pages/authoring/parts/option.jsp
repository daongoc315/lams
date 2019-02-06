<input type="hidden" name="optionSequenceId${status.index}" value="${option.sequenceId}">
<input type="hidden" name="optionUid${status.index}" value="${option.uid}">

<div class="option-ckeditor">
	<c:set var="OPTION_LABEL"><fmt:message key="label.authoring.basic.option.answer"/></c:set>
	<lams:CKEditor id="optionString${status.index}" value="${option.optionString}" 
		placeholder="${OPTION_LABEL}&thinsp; ${status.index+1}" contentFolderID="${contentFolderID}" height="50px"/>
</div>

<div class="settings-on-hover-hidden">
	<%@ include file="gradeselector.jsp"%>

	<div class="voffset5-bottom">
    	<c:set var="FEEDBACK_LABEL"><fmt:message key="label.authoring.basic.option.feedback"/></c:set>
     	<lams:CKEditor id="optionFeedback${status.index}" value="${option.feedback}" 
	     	placeholder="${FEEDBACK_LABEL}" contentFolderID="${contentFolderID}" height="50px"/>
	</div>
</div>