<%@ include file="/common/taglibs.jsp"%>
<c:set var="formBean" value="<%=request.getAttribute(org.apache.struts.taglib.html.Constants.BEAN_KEY)%>" />
<c:set var="sessionMapID" value="${formBean.sessionMapID}" />
<c:url var="newQuestionInitUrl" value='/authoring/newQuestionInit.do'>
	<c:param name="sessionMapID" value="${sessionMapID}" />
</c:url>	

<script lang="javascript">
	$(document).ready(function(){
		reinitializePassingMarkSelect(true);
	});

	//The panel of assessment list panel
	var questionListTargetDiv = "#questionListArea";
	function deleteQuestion(idx){
		var	deletionConfirmed = confirm("<fmt:message key="warning.msg.authoring.do.you.want.to.delete"></fmt:message>");

		if (deletionConfirmed) {
			var url = "<c:url value="/authoring/removeQuestion.do"/>";
			$(questionListTargetDiv).load(
				url,
				{
					questionIndex: idx, 
					sessionMapID: "${sessionMapID}",
					referenceGrades: serializeReferenceGrades()
				},
				function(){
					reinitializePassingMarkSelect(false);
					refreshThickbox();
				}
			);
		};
	}
	function addQuestionReference(){
		var questionTypeDropdown = document.getElementById("questionSelect");
		var idx = questionTypeDropdown.value;
		
		var url = "<c:url value="/authoring/addQuestionReference.do"/>";
		$(questionListTargetDiv).load(
				url,
				{
					questionIndex: idx, 
					sessionMapID: "${sessionMapID}",
					referenceGrades: serializeReferenceGrades()
				},
				function(){
					reinitializePassingMarkSelect(false);
					refreshThickbox();
				}
		);
	}
	function deleteQuestionReference(idx){
		var url = "<c:url value="/authoring/removeQuestionReference.do"/>";
		$(questionListTargetDiv).load(
			url,
			{
				questionReferenceIndex: idx, 
				sessionMapID: "${sessionMapID}",
				referenceGrades: serializeReferenceGrades()
			},
			function(){
				reinitializePassingMarkSelect(false);
				refreshThickbox();
			}
		);
	}
	function upQuestionReference(idx){
		var url = "<c:url value="/authoring/upQuestionReference.do"/>";
		$(questionListTargetDiv).load(
				url,
				{
					questionReferenceIndex: idx, 
					sessionMapID: "${sessionMapID}",
					referenceGrades: serializeReferenceGrades()
				},
				function(){
					refreshThickbox();
				}
		);
	}
	function downQuestionReference(idx){
		var url = "<c:url value="/authoring/downQuestionReference.do"/>";
		$(questionListTargetDiv).load(
				url,
				{
					questionReferenceIndex: idx, 
					sessionMapID: "${sessionMapID}",
					referenceGrades: serializeReferenceGrades()
				},
				function(){
					refreshThickbox();
				}
		);
	}
	function serializeReferenceGrades(){
		var serializedGrades = "";
		$("[name^=grade]").each(function() {
			serializedGrades += "&" + this.name + "="  + this.value;
		});
		return serializedGrades;
	}
	
	function exportQuestions(){   
	    var reqIDVar = new Date();
		var param = "?sessionMapID=${sessionMapID}&reqID="+reqIDVar.getTime();
		location.href="<c:url value='/authoring/exportQuestions.do'/>" + param;
	};

	function resizeIframe() {
		if (document.getElementById('TB_iframeContent') != null) {
		    var height = top.window.innerHeight;
		    if ( height == undefined || height == 0 ) {
		    	// IE doesn't use window.innerHeight.
		    	height = document.documentElement.clientHeight;
		    	// alert("using clientHeight");
		    }
			// alert("doc height "+height);
		    height -= document.getElementById('TB_iframeContent').offsetTop + 60;
		    document.getElementById('TB_iframeContent').style.height = height +"px";
	
			TB_HEIGHT = height + 28;
			tb_position();
		}
	};
	window.onresize = resizeIframe;

	function createNewQuestionInitHref() {
		var questionTypeDropdown = document.getElementById("questionType");
		var questionType = questionTypeDropdown.selectedIndex + 1;
		var newQuestionInitHref = "${newQuestionInitUrl}&questionType=" + questionType + "&referenceGrades=" + encodeURIComponent(serializeReferenceGrades()) + "&KeepThis=true&TB_iframe=true&height=640&width=950&modal=true";
		$("#newQuestionInitHref").attr("href", newQuestionInitHref)
	};
	function refreshThickbox(){
		tb_init('a.thickbox, area.thickbox, input.thickbox');//pass where to apply thickbox
	};
	function reinitializePassingMarkSelect(isPageFresh){
		var oldValue = (isPageFresh) ? "${formBean.assessment.passingMark}" : $("#passingMark").val();
		$('#passingMark').empty();
		$('#passingMark').append( new Option("<fmt:message key='label.authoring.advance.passing.mark.none' />",0) );
		
		var maxGrade = 0;
		$("[name^=grade]").each(function() {
			maxGrade += eval(this.value);
		});
		
		for (var i = 1; i<=maxGrade; i++) {
			var isSelected = (oldValue == i);
		    $('#passingMark').append( new Option(i,i,isSelected) );
		}
	};	


</script>

<!-- Basic Tab Content -->
<table>
	<tr>
		<td colspan="2">
			<div class="field-name">
				<fmt:message key="label.authoring.basic.title"></fmt:message>
			</div>
			<html:text property="assessment.title" style="width: 99%;"></html:text>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="field-name">
				<fmt:message key="label.authoring.basic.instruction"></fmt:message>
			</div>
			<lams:CKEditor id="assessment.instructions" value="${formBean.assessment.instructions}"
				contentFolderID="${formBean.contentFolderID}">
			</lams:CKEditor>
		</td>
	</tr>

</table>

<div id="questionListArea">
	<c:set var="sessionMapID" value="${formBean.sessionMapID}" />
	<%@ include file="/pages/authoring/parts/questionlist.jsp"%>
</div>

<!-- Dropdown menu for choosing a question type -->
<p>
	<select id="questionType" style="float: left">
		<option selected="selected"><fmt:message key="label.authoring.basic.type.multiple.choice" /></option>
		<option><fmt:message key="label.authoring.basic.type.matching.pairs" /></option>
		<option><fmt:message key="label.authoring.basic.type.short.answer" /></option>
		<option><fmt:message key="label.authoring.basic.type.numerical" /></option>
		<option><fmt:message key="label.authoring.basic.type.true.false" /></option>
		<option><fmt:message key="label.authoring.basic.type.essay" /></option>
		<option><fmt:message key="label.authoring.basic.type.ordering" /></option>
	</select>
	
	<a onclick="createNewQuestionInitHref();return false;" href="" class="button-add-item space-left thickbox" id="newQuestionInitHref">  
		<fmt:message key="label.authoring.basic.add.question.to.pool" />
	</a>
	
	<c:set var="importInitUrl" >
		<c:url value='/authoring/importInit.do'/>?sessionMapID=${sessionMapID}&KeepThis=true&TB_iframe=true&height=240&width=650
	</c:set>
	<a href="${importInitUrl}" class="button space-right thickbox" id="importButton" style="float: right;">  
		<fmt:message key="label.authoring.basic.import.questions" />
	</a>
	
	<a onclick="javascript:exportQuestions();" class="button space-right" id="exportButton" style="float: right;">  
		<fmt:message key="label.authoring.basic.export.questions" />
	</a>
</p>
<br>
