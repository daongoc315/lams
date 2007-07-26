﻿/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ************************************************************************
 */

import org.lamsfoundation.lams.authoring.cv.*;
import org.lamsfoundation.lams.authoring.br.BranchConnector;
import org.lamsfoundation.lams.authoring.*;
import org.lamsfoundation.lams.common.util.*
import org.lamsfoundation.lams.common.dict.*
import org.lamsfoundation.lams.common.style.*
import org.lamsfoundation.lams.common.ui.*
import org.lamsfoundation.lams.common.*

import mx.controls.*
import mx.utils.*
import mx.managers.*
import mx.events.*

/*
*
* @author      DC
* @version     0.1
* @comments    Property Inspector Controls for the canvas
* 
*/
class PropertyInspectorControls extends MovieClip {
	
	private var _canvasModel:CanvasModel;
	private var _canvasController:CanvasController;
	private var _dictionary:Dictionary;
	
	 //References to components + clips 
   	private var _piIsExpended:Boolean;
    private var _tm:ThemeManager;
	private var _app:ApplicationParent;
	
    private var toolDisplayName_lbl:Label;
	private var clickTarget_mc:MovieClip;
	private var _depth:Number;
	private var delimitLine:MovieClip;
	private var expand_mc:MovieClip;
	private var collapse_mc:MovieClip;
	
	private static var ICON_OFFSET:Number = 20;
	
	//Properties tab
    private var title_lbl:Label;
    private var title_txt:TextInput;
    private var desc_lbl:Label;
    private var desc_txt:TextInput;
    private var piHeightHide:Number = 23;
	private var piHeightFull:Number = 106;
	private var grouping_lbl:Label;
	private var applied_grouping_lbl:Label;
	private var currentGrouping_lbl:Label;
	private var appliedGroupingActivity_cmb:ComboBox;
	
	private var editGrouping_btn:Button;
    private var runOffline_chk:CheckBox;
    private var defineLater_chk:CheckBox;
	
	//genral info
	private var total_num_activities_lbl:Label;
	
	//gates
	private var gateType_lbl:Label;
	private var gateType_cmb:ComboBox;
	private var startOffset_lbl:Label;
	private var endOffset_lbl:Label;
	private var days_lbl:Label;
	private var hours_lbl:Label;
	private var mins_lbl:Label;
	private var hoursEnd_lbl:Label;
	private var minsEnd_lbl:Label;
	private var days_stp:NumericStepper;
	private var hours_stp:NumericStepper;
	private var mins_stp:NumericStepper;
	private var endHours_stp:NumericStepper;
	private var endMins_stp:NumericStepper;
	
	//branches
	private var branchType_lbl:Label;
	private var branchType_cmb:ComboBox;
	private var _group_match_btn:Button;

	//grouping 
	private var groupType_lbl:Label;
	private var numGroups_lbl:Label;
	private var numLearners_lbl:Label;
	private var groupType_cmb:ComboBox;
	private var numGroups_rdo:RadioButton;
	private var numLearners_rdo:RadioButton;
	private var rndGroup_radio:RadioButtonGroup;
	private var numGroups_stp:NumericStepper;
	private var numRandomGroups_stp:NumericStepper;
	private var numLearners_stp:NumericStepper;
	
	private var _group_naming_btn:Button;
	
	//Complex Activity
	private var min_lbl:Label;
	private var max_lbl:Label;
	private var minAct_stp:NumericStepper;
	private var maxAct_stp:NumericStepper;
	
	//screen assets:
	private var body_pnl:Panel;
	private var bar_pnl:Panel;
	private var cover_pnl:Panel;
	
	//Defined so compiler can 'see' events added at runtime by EventDispatcher
    private var dispatchEvent:Function;     
    public var addEventListener:Function;
    public var removeEventListener:Function;
	
	
	/**
	 * Constructor
	 */
 
	public function PropertyInspectorControls(){
		//register to recive updates form the model
		Debugger.log('Constructor',Debugger.GEN,'PropertyInspector','PropertyInspector');
		
		_tm = ThemeManager.getInstance();
		_app = ApplicationParent.getInstance();
		
		//Set up this class to use the Flash event delegation model
		EventDispatcher.initialize(this);
		
		_dictionary = Dictionary.getInstance();
		
	}

	
	public function setupLabels(){
		toolDisplayName_lbl.text = "<b>"+Dictionary.getValue('pi_title')+"</b>"
		
		gateType_lbl.text = Dictionary.getValue('trans_dlg_gatetypecmb');
		branchType_lbl.text = Dictionary.getValue("pi_branch_type");
		
		days_lbl.text = Dictionary.getValue('pi_days');
		hours_lbl.text = Dictionary.getValue('pi_hours');
		mins_lbl.text = Dictionary.getValue('pi_mins');
		hoursEnd_lbl.text = Dictionary.getValue('pi_hours');
		minsEnd_lbl.text = Dictionary.getValue('pi_mins');
		startOffset_lbl.text = Dictionary.getValue('pi_start_offset');
		endOffset_lbl.text = Dictionary.getValue('pi_end_offset');
		
		groupType_lbl.text = Dictionary.getValue('pi_group_type');
		numGroups_lbl.text = Dictionary.getValue('pi_num_groups');
		numLearners_lbl.text = Dictionary.getValue('pi_num_learners');
		
		//Properties tab
		title_lbl.text = Dictionary.getValue('pi_lbl_title');
		desc_lbl.text = Dictionary.getValue('pi_lbl_desc');
		grouping_lbl.text = Dictionary.getValue('pi_lbl_group');
		currentGrouping_lbl.text = Dictionary.getValue('pi_lbl_currentgroup');
		defineLater_chk.label = Dictionary.getValue('pi_definelater');
		runOffline_chk.label = Dictionary.getValue('pi_runoffline');
				
		//Complex Activity
		min_lbl.text = Dictionary.getValue('pi_min_act');
		max_lbl.text = Dictionary.getValue('pi_max_act');
		
		_group_match_btn.label = Dictionary.getValue('pi_group_match_btn_lbl');
		_group_naming_btn.label = Dictionary.getValue('pi_group_naming_btn_lbl');
		
		//populate the synch type combo:
		gateType_cmb.dataProvider = Activity.getGateActivityTypes();
		branchType_cmb.dataProvider = Activity.getBranchingActivityTypes();
		groupType_cmb.dataProvider = Grouping.getGroupingTypesDataProvider();
		
		//Call to apply style to all the labels and input fields
		setStyles();
		
		//fire event to say we have loaded
		delete this.onEnterFrame; 
		
		//hide all the controls at startup
		delimitLine._visible = false;
		_group_match_btn.visible = false;
		
		hideAllSteppers(false);
		showGroupingControls(false);
		showGeneralControls(false);
		showOptionalControls(false);
		showToolActivityControls(false);
		showGateControls(false);
		showBranchingControls(false);
		showAppliedGroupingControls(false);
		showGeneralInfo(true);
		
		dispatchEvent({type:'load',target:this});
	}
	
	private function hideAllSteppers(v):Void{ 
		hours_stp.visible = v
		mins_stp.visible = v
		endHours_stp.visible = v
		endMins_stp.visible = v
		numGroups_stp.visible = v
		numRandomGroups_stp.visible = v
		numLearners_stp.visible = v
		minAct_stp.visible = v
		maxAct_stp.visible = v
	}
	
	private function setTabIndex(selectedTab:String){
		
		//Tool Activities
		title_txt.tabIndex = 401
		applied_grouping_lbl.tabIndex = 402
		appliedGroupingActivity_cmb.tabIndex = 402
		runOffline_chk.tabIndex = 403
		defineLater_chk.tabIndex = 404
		
		//Optional Activities
		desc_txt.tabIndex = 402
		minAct_stp.tabIndex = 403
		maxAct_stp.tabIndex = 404
		
		//Gate Activities
		//gateType_cmb.enabled = true
		gateType_cmb.tabIndex = 402
		days_stp.tabIndex = 403
		hours_stp.tabIndex = 404
		mins_stp.tabIndex = 405
		endHours_stp.tabIndex = 406
		endMins_stp.tabIndex = 407
		
		//Grouping Activities
		groupType_cmb.tabIndex = 402
		numGroups_stp.tabIndex = 403
		numGroups_rdo.tabIndex = 404
		numRandomGroups_stp.tabIndex = 405
		numLearners_rdo.tabIndex = 406
		numLearners_stp.tabIndex = 407
		
	}
	
	private function showToolActivityControls(v:Boolean, e:Boolean){
		
		var a = _canvasModel.selectedItem.activity;
		var parentAct = _canvasModel.getCanvas().ddm.getActivityByUIID(a.parentUIID)
		
		if(a.parentUIID != null && parentAct.activityTypeID == Activity.PARALLEL_ACTIVITY_TYPE) {
			applied_grouping_lbl.visible = v
			appliedGroupingActivity_cmb.visible = false;
			
			applied_grouping_lbl.enabled = e;
			
		} else {
			applied_grouping_lbl.visible = false
			appliedGroupingActivity_cmb.visible = v;
			
			appliedGroupingActivity_cmb.enabled = e;
			
		}
		
		grouping_lbl.visible = v;
		currentGrouping_lbl.visible = v;
		runOffline_chk.visible = v;
		defineLater_chk.visible = v;
		editGrouping_btn.visible = v;
		
		if(e != null) {
			grouping_lbl.enabled = e;
			currentGrouping_lbl.enabled = e;
			runOffline_chk.enabled = e;
			defineLater_chk.enabled = e;
			editGrouping_btn.enabled = e;
		}
	}
	
	
	private function showGeneralInfo(v:Boolean, e:Boolean){
		total_num_activities_lbl.visible = v;
		total_num_activities_lbl.enabled = (e != null) ? e : true;
	} 
	
	private function showGeneralControls(v:Boolean, e:Boolean){

		title_lbl.visible = v;
		title_txt.visible = v;
		
		if(e != null) {
			title_lbl.enabled = e;
			title_txt.enabled = e;
		}
	} 
	
	private function showOptionalControls(v:Boolean, e:Boolean){
		
		min_lbl.visible = v;	
		max_lbl.visible = v;
		minAct_stp.visible = v;
		maxAct_stp.visible = v;
		desc_lbl.visible = v;
		desc_txt.visible = v;
		
		if(e != null) {
			min_lbl.enabled = e;	
			max_lbl.enabled = e;
			minAct_stp.enabled = e;
			maxAct_stp.enabled = e;
			desc_lbl.enabled = e;
			desc_txt.enabled = e;
		}
		
		grouping_lbl.visible = false;
		
		
	}
	
	private function showGateControls(v:Boolean, e:Boolean){
		
		days_lbl.visible = v;
		hours_lbl.visible = v;
		mins_lbl.visible = v;
		hoursEnd_lbl.visible = v;
		minsEnd_lbl.visible = v;
		days_stp.visible = v;
		hours_stp.visible = v;
		mins_stp.visible = v;
		gateType_lbl.visible = v;
		gateType_cmb.visible = v;
		startOffset_lbl.visible = v;
		
		if(e != null) {
			days_lbl.enabled = e;
			hours_lbl.enabled = e;
			mins_lbl.enabled = e;
			hoursEnd_lbl.enabled = e;
			minsEnd_lbl.enabled = e;
			days_stp.enabled = e;
			hours_stp.enabled = e;
			mins_stp.enabled = e;
			gateType_lbl.enabled = e;
			gateType_cmb.enabled = e;
			startOffset_lbl.enabled = e;
		}
		
	}
	
	private function showBranchingControls(v:Boolean, e:Boolean){
		branchType_lbl.visible = v;
		branchType_cmb.visible = v;
		
		if(_group_match_btn.visible && !v) _group_match_btn.visible = v;
		
		if(e != null) {
			branchType_lbl.enabled = e;
			branchType_cmb.enabled = e;
		}
	}
	
	/**
	 * Shows or hides the app.lied grouping
	 * AND title fields
	 * @usage   
	 * @param   v 
	 * @return  
	 */
	private function showAppliedGroupingControls(v:Boolean, e:Boolean){
		if(_canvasModel.selectedItem.activity.activityTypeID != Activity.GROUP_BRANCHING_ACTIVITY_TYPE) {
			grouping_lbl._y = 52;
			appliedGroupingActivity_cmb._y = 52;
		} else {
			grouping_lbl._y = 75;
			appliedGroupingActivity_cmb._y = 75;
		}
			
		grouping_lbl.visible = v;
		appliedGroupingActivity_cmb.visible = v;
		
		if(e != null) {
			grouping_lbl.enabled = e;
			appliedGroupingActivity_cmb.enabled = e;
		}
	
	}
	
	private function checkEnabledGroupControl(){
		var ca = _canvasModel.selectedItem;
		var parentAct = _canvasModel.getCanvas().ddm.getActivityByUIID(ca.activity.parentUIID)
		if (ca.activity.parentUIID != null && parentAct.activityTypeID == Activity.PARALLEL_ACTIVITY_TYPE) {
			appliedGroupingActivity_cmb.enabled = false;
		}else {
			appliedGroupingActivity_cmb.enabled = false;
		}
	}
	
	private function showGroupingControls(v:Boolean, e:Boolean){
		groupType_lbl.visible = v;
		groupType_cmb.visible = v;
		
		if(v){
			showRelevantGroupOptions();
		}else{
			numGroups_lbl.visible = v;
			numLearners_lbl.visible = v;
			numGroups_rdo.visible = v;
			numLearners_rdo.visible = v;
			numGroups_stp.visible = v;
			numRandomGroups_stp.visible = v;
			numLearners_stp.visible = v;
			_group_naming_btn.visible = v;
		}
		
	}
		
	private function showRelevantGroupOptions(e:Boolean){
		
		var ga = _canvasModel.selectedItem.activity;
		var g = _canvasModel.getCanvas().ddm.getGroupingByUIID(ga.createGroupingUIID);
		Debugger.log('g.groupingTypeID:'+g.groupingTypeID,Debugger.GEN,'showRelevantGroupOptions','org.lamsfoundation.lams.common.cv.PropertyInspector');
		Debugger.log('Grouping.CHOSEN_GROUPING:'+Grouping.CHOSEN_GROUPING,Debugger.GEN,'showRelevantGroupOptions','org.lamsfoundation.lams.common.cv.PropertyInspector');
		Debugger.log('Grouping.RANDOM_GROUPING:'+Grouping.RANDOM_GROUPING,Debugger.GEN,'showRelevantGroupOptions','org.lamsfoundation.lams.common.cv.PropertyInspector');
		
		if(g.groupingTypeID == Grouping.CHOSEN_GROUPING){
			numGroups_lbl.visible = true;
			numLearners_lbl.visible = false;
			numGroups_stp.visible = true;
			numRandomGroups_stp.visible = false;
			numLearners_stp.visible = false;
			numLearners_rdo.visible = false;
			numGroups_rdo.visible = false;
			
			_group_naming_btn.visible = true;
			
			if(e != null) {
				numGroups_lbl.enabled = e;
				_group_naming_btn.enabled = e;
			}
			
		}else if(g.groupingTypeID == Grouping.RANDOM_GROUPING){
			numGroups_lbl.visible = true;
			numLearners_lbl.visible = true;
			numGroups_stp.visible = false;
			numRandomGroups_stp.visible = true;
			numLearners_stp.visible = true;
			numLearners_rdo.visible = true;
			numGroups_rdo.visible = true;
			
			_group_naming_btn.visible = true;
			
			if(e != null) {
				numGroups_lbl.enabled = e;
				numLearners_lbl.enabled = e;
				numGroups_stp.enabled = e;
				numRandomGroups_stp.enabled = e;
				numLearners_stp.enabled = e;
				numLearners_rdo.enabled = e;
				numGroups_rdo.enabled = e;
				
				_group_naming_btn.enabled = e;
			}
			
			
			checkEnableGroupsOptions(e);
			
		}else{
			//error dont understand the grouping type
		}
		
		
	}
	
	private function reDrawTroublesomeSteppers(e:Boolean){
		numLearners_stp.visible = true;
		numRandomGroups_stp.visible = true;
		
		
	}
	
	private function checkEnableGateControls(e:Boolean){
		if(_canvasModel.selectedItem.activity.activityTypeID == Activity.SCHEDULE_GATE_ACTIVITY_TYPE){
			if(e != null) {
				days_stp.enabled = e;
				hours_stp.enabled = e;
				mins_stp.enabled = e;
				endHours_stp.enabled = e;
				endMins_stp.enabled = e;
			} else {
				days_stp.enabled = true;
				hours_stp.enabled = true;
				mins_stp.enabled = true;
				endHours_stp.enabled = true;
				endMins_stp.enabled = true;
			}
					
		}
		/**/
		else{
			days_stp.enabled = false;
			hours_stp.enabled = false;
			mins_stp.enabled = false;
			endHours_stp.enabled = false;
			endMins_stp.enabled = false;
		}
		
		//this is a crazy hack to stop the steppter dissapearing after its .enabled property is set.
		//waits 2 frames to re-display the control ( 1 doensnt work!)
		MovieClipUtils.doLater(Proxy.create(this,showGateControlsLater,true));
	}
	
	public function showGateControlsLater(bool:Boolean){
		MovieClipUtils.doLater(Proxy.create(this,showGateControls,bool));
	}
	
	/**
	 * Enables or disables the grouping method steppers
	 * @usage   
	 * @return  
	 */
	private function checkEnableGroupsOptions(e:Boolean){
		var groupingBy = rndGroup_radio.selection.data;
		Debugger.log('groupingBy:'+groupingBy,Debugger.GEN,'checkEnableGroupsOptions','PropertyInspector');
		if(groupingBy == 'num_learners'){
			numRandomGroups_stp.value = 0;
			numRandomGroups_stp.enabled = false;
			numLearners_stp.enabled = (e != null) ? e :true;
			
			_group_naming_btn.enabled = false;
		}else{
			numRandomGroups_stp.enabled = (e != null) ? e : true;
			numLearners_stp.value = 0;
			numLearners_stp.enabled = false;
			
			_group_naming_btn.enabled = (e != null && numRandomGroups_stp.value > 0) ? e : true;
		}
		
		//this is a crazy hack to stop the steppter dissapearing after its .enabled property is set.
		//waits 2 frames to re-display the control ( 1 doensnt work!)
		MovieClipUtils.doLater(Proxy.create(this,reDrawTroublesomeSteppersLater));
		
	}
	
	public function reDrawTroublesomeSteppersLater(){
		MovieClipUtils.doLater(Proxy.create(this,reDrawTroublesomeSteppers));
	}
	
	/**
	 * Called when there is a change in the values of the group method steppers
	 * Butdates the value inthe grouping class being edited.
	 * @usage   
	 * @return  
	 */
	public function updateGroupingMethodData(){
		var ga = _canvasModel.selectedItem.activity;
		var g = _canvasModel.getCanvas().ddm.getGroupingByUIID(ga.createGroupingUIID);
		if(g.groupingTypeID == Grouping.RANDOM_GROUPING){
			//not only one of these should actually have a non 0 value
			g.learnersPerGroup = numLearners_stp.value;
			g.numberOfGroups = numRandomGroups_stp.value;
		}else{
			g.maxNumberOfGroups = numGroups_stp.value;
		}
		
		setModified();
	}
	
	private function getGroupingActivitiesDP(){
		var gActs = _canvasModel.getCanvas().ddm.getGroupingActivities();
		var gActsDP = new Array();
		var ga = _canvasModel.selectedItem.activity;
		var g = _canvasModel.getCanvas().ddm.getGroupingByUIID(ga.createGroupingUIID);
		
		gActsDP.push({label:Dictionary.getValue('pi_no_grouping'),data:null});
		
		for(var i=0; i<gActs.length;i++){
			if (ga.createGroupingUIID != gActs[i].createGroupingUIID){
				gActsDP.push({label:gActs[i].title,data:gActs[i]});
			}
		}
		
		return gActsDP;
	}
	
	
	

	/**
    * Main resize method, called by scrollpane container/parent
    */
    public function setSize(w:Number,h:Number){
      
        //Size the bkg_pnl
		clickTarget_mc._width = w;
        body_pnl.setSize(w,h-bar_pnl.height);
		cover_pnl.setSize(w,h-bar_pnl.height);
        bar_pnl.setSize(w);
		
		// size the expand/colapse icon
		expand_mc._x = w - ICON_OFFSET;
		collapse_mc._x = w - ICON_OFFSET;
		expand_mc.redraw(true);
		collapse_mc.redraw(true);
    }
	
	
	
	/**
	 * Get the CSSStyleDeclaration objects for each component and applies them
	 * directly to the instanced
	 * @usage   
	 * @return  
	 */
	private function setStyles() {
       // Debugger.log('Button stykle obejct',Debugger.GEN,'setStyles','PropertyInspector');
		var styleObj = _tm.getStyleObject('button');
		editGrouping_btn.setStyle('styleName',styleObj);
		_group_match_btn.setStyle('styleName',styleObj);
		_group_naming_btn.setStyle('styleName', styleObj);
		
		styleObj = _tm.getStyleObject('PIlabel');
		
		toolDisplayName_lbl.setStyle('styleName',styleObj);
		title_lbl.setStyle('styleName',styleObj);
		desc_lbl.setStyle('styleName',styleObj);
		min_lbl.setStyle('styleName',styleObj);
		max_lbl.setStyle('styleName',styleObj);
		grouping_lbl.setStyle('styleName',styleObj);
		currentGrouping_lbl.setStyle('styleName',styleObj);
		gateType_lbl.setStyle('styleName',styleObj);
		branchType_lbl.setStyle('styleName',styleObj);
		startOffset_lbl.setStyle('styleName',styleObj);
		endOffset_lbl.setStyle('styleName',styleObj);
		days_lbl.setStyle('styleName', styleObj);
		hours_lbl.setStyle('styleName',styleObj);
		mins_lbl.setStyle('styleName',styleObj);
		hoursEnd_lbl.setStyle('styleName',styleObj);
		minsEnd_lbl.setStyle('styleName',styleObj);
		numGroups_lbl.setStyle('styleName',styleObj);
		numLearners_lbl.setStyle('styleName',styleObj);
		groupType_lbl.setStyle('styleName',styleObj);
		applied_grouping_lbl.setStyle('styleName',styleObj);
		title_txt.setStyle('styleName',styleObj);
		desc_txt.setStyle('styleName',styleObj);
		runOffline_chk.setStyle('styleName',styleObj);
		defineLater_chk.setStyle('styleName',styleObj);
		days_stp.setStyle('styleName', styleObj);
		hours_stp.setStyle('styleName',styleObj);
		mins_stp.setStyle('styleName',styleObj);
		endHours_stp.setStyle('styleName',styleObj);
		endMins_stp.setStyle('styleName',styleObj);
		minAct_stp.setStyle('styleName',styleObj);
		maxAct_stp.setStyle('styleName',styleObj);
		
		
		styleObj = _tm.getStyleObject('picombo');
		gateType_cmb.setStyle('styleName',styleObj);
		branchType_cmb.setStyle('styleName', styleObj);
		groupType_cmb.setStyle('styleName',styleObj);
		appliedGroupingActivity_cmb.setStyle('styleName',styleObj);
		
		styleObj = _tm.getStyleObject('numericstepper');
		days_stp.setStyle('styleName',styleObj);
		hours_stp.setStyle('styleName',styleObj);
		mins_stp.setStyle('styleName',styleObj);
		endHours_stp.setStyle('styleName',styleObj);
		endMins_stp.setStyle('styleName',styleObj);
		
		styleObj = _tm.getStyleObject('BGPanel');
		bar_pnl.setStyle('styleName',styleObj);
		
		styleObj = _tm.getStyleObject('WZPanel');
		body_pnl.setStyle('styleName',styleObj);
		cover_pnl.setStyle('styleName',styleObj);
		
		
    }
    
  
	/////////////////////////////////////////////////
	//------------ controller section -------------//
	/////////////////////////////////////////////////
	
	/**
	 *Handles change event fired from the gateType_cmb
	 * @usage   
	 * @param   evt 
	 * @return  
	 */
	private function onGateTypeChange(evt:Object){
		_canvasModel.selectedItem.activity.activityTypeID = evt.target.value;
		Debugger.log('Set gate type to: _canvasModel.selectedItem.activity.activityTypeID:'+_canvasModel.selectedItem.activity.activityTypeID,Debugger.GEN,'onGateTypeChange','PropertyInspector');
		checkEnableGateControls(!_canvasModel.selectedItem.activity.readOnly);
		
		setModified();
	}
	
	/**
	 *Handles change event fired from the branchType_cmb
	 * @usage   
	 * @param   evt 
	 * @return  
	 */
	private function onBranchTypeChange(evt:Object){
		_canvasModel.selectedItem.activity.activityTypeID = evt.target.value;
		Debugger.log('Set branch type to: _canvasModel.selectedItem.activity.activityTypeID:'+_canvasModel.selectedItem.activity.activityTypeID,Debugger.GEN,'onBranchTypeChange','PropertyInspector');
		
		if(evt.target.value == Activity.GROUP_BRANCHING_ACTIVITY_TYPE) {
			var selectedGroup = appliedGroupingActivity_cmb.dataProvider[appliedGroupingActivity_cmb.selectedIndex].data;
			if(selectedGroup != null) {
				_canvasModel.selectedItem.activity.groupingUIID = selectedGroup.createGroupingUIID;
			}
				
			showGroupBasedBranchingControls(true, !_canvasModel.selectedItem.activity.readOnly);
			showAppliedGroupingControls(true, !_canvasModel.selectedItem.activity.readOnly);
		} else {

			_canvasModel.selectedItem.activity.groupingUIID = null;
			showGroupBasedBranchingControls(false);
			showAppliedGroupingControls(false);
		}
		
		_canvasModel.selectedItem.refresh();
		
		setModified();
	}
	
	/**
	 * Handles change event fired from the groupType_cmb
	 * @usage   
	 * @param   evt 
	 * @return  
	 */
	private function onGroupTypeChange(evt:Object){
		var ga = _canvasModel.selectedItem.activity;
		var g = _canvasModel.getCanvas().ddm.getGroupingByUIID(ga.createGroupingUIID);
		g.groupingTypeID = evt.target.value;
		Debugger.log('Set group type to: '+g.groupingTypeID,Debugger.GEN,'onGroupTypeChange','PropertyInspector');
		showRelevantGroupOptions(!ga.readOnly);
		
		setModified();
	}
	
	private function onAppliedGroupingChange(evt:Object){
		var a = _canvasModel.selectedItem;
		
		//get the groupingUIID of the grouping Actviity we have selected.
		var newGroupingUIID = evt.target.value.createGroupingUIID;
		a.activity.groupingUIID = newGroupingUIID;
		if (a.activity.activityTypeID == Activity.PARALLEL_ACTIVITY_TYPE){
			for (var k=0; k<a.actChildren.length; k++){
				a.actChildren[k].groupingUIID = newGroupingUIID;
			}
			a.init();
		}
		
		Debugger.log('Set grouping UIID to: '+a.activity.groupingUIID ,Debugger.GEN,'onAppliedGroupingChange','PropertyInspector');
		a.refresh()
		
		if(a.activity.activityTypeID == Activity.GROUP_BRANCHING_ACTIVITY_TYPE) {
			showGroupBasedBranchingControls(true, !_canvasModel.selectedItem.activity.readOnly);
		}
		
		setModified();
	}
	
	private function showGroupBasedBranchingControls(v:Boolean, e:Boolean) {
		var ca = _canvasModel.selectedItem;
		var branches:Object = _canvasModel.getCanvas().ddm.getBranchesForActivityUIID(ca.activity.activityUIID);
		
		if(branches.myBranches.length > 0 && ca.activity.groupingUIID != null) {
			_group_match_btn.visible = v;
			_group_match_btn.enabled = e;
		} else {
			_group_match_btn.visible = false;
		}
		
	}
	
	/**
	 * Handles click event from the grouping method radio buttons
	 * @usage   
	 * @param   evt 
	 * @return  
	 */
	private function onGroupingMethodChange(evt:Object){
		checkEnableGroupsOptions(!_canvasModel.selectedItem.activity.readOnly);
		setModified();
	}
	
	
	private function onScheduleOffsetChange(evt:Object){
		
		var startOffsetMins:Number = (days_stp.value * 60 * 24) + (hours_stp.value * 60) + mins_stp.value;
		_canvasModel.selectedItem.activity.gateStartTimeOffset = startOffsetMins;
		var endOffsetMins:Number = (endHours_stp.value * 60) + endMins_stp.value;
		_canvasModel.selectedItem.activity.gateEndTimeOffset = endOffsetMins;
		Debugger.log('activity.gateStartTimeOffset :'+_canvasModel.selectedItem.activity.gateStartTimeOffset ,Debugger.GEN,'onScheduleOffsetChange','PropertyInspector');
		Debugger.log('activity.gateEndTimeOffset :'+_canvasModel.selectedItem.activity.gateEndTimeOffset,Debugger.GEN,'onScheduleOffsetChange','PropertyInspector');
		
		setModified();
		
	}
	
	private function onGroupMatchClick(evt:Object){
		
		// open group to branch matching window
		_app.dialog = PopUpManager.createPopUp(Application.root, LFWindow, true, {title:Dictionary.getValue('groupmatch_dlg_title_lbl'), closeButton:true, viewResize:false, scrollContentPath:'groupMatchDialog'});
		_app.dialog.addEventListener('contentLoaded', Delegate.create(this, groupMatchDialogLoaded));
		
		setModified();
	}
	
	private function onGroupNamingClick(evt:Object){
		
		// open group to branch matching window
		_app.dialog = PopUpManager.createPopUp(Application.root, LFWindow, true, {title:Dictionary.getValue('groupnaming_dlg_title_lbl'), closeButton:true, viewResize:false, scrollContentPath:'GroupNamingDialog'});
		_app.dialog.addEventListener('contentLoaded', Delegate.create(this, GroupNamingDialogLoaded));
		
		setModified();
	}
	
	private function groupMatchDialogLoaded(evt:Object) {
		var branches:Object = _canvasModel.getCanvas().ddm.getBranchesForActivityUIID(_canvasModel.selectedItem.activity.activityUIID);
		var grouping = _canvasModel.getCanvas().ddm.getGroupingByUIID(_canvasModel.selectedItem.activity.groupingUIID);
		
		evt.target.scrollContent.groups = grouping.getGroups();
		evt.target.scrollContent.branches = getValidBranches(branches.myBranches);
		evt.target.scrollContent.loadLists();
		
	}
	
	private function GroupNamingDialogLoaded(evt:Object) {
		var grouping = _canvasModel.getCanvas().ddm.getGroupingByUIID(_canvasModel.selectedItem.activity.createGroupingUIID);
		
		Debugger.log("grouping: " + grouping, Debugger.CRITICAL, "GroupNamingDialogLoaded", "PropertyInspectorControls");
		
		evt.target.scrollContent.groups = grouping.getGroups();
		evt.target.scrollContent.setupGrid();
	}
	
	private function getValidBranches(branches:Array):Array {
		for(var i=0; i < branches.length; i++) {
			if(branches[i].direction != BranchConnector.DIR_FROM_START)
				branches.splice(i, 1);
		}
		
		return branches;
	}
	
   /**
	 * Recieves the click events from the canvas views (inc Property Inspector) buttons.  Based on the target
	 * the relevent method is called to action the user request
	 * @param   evt 
	 */
	 /**/
	public function click(e):Void{
		var tgt:String = new String(e.target);
		if(tgt.indexOf("defineLater_chk") != -1){
			
			_canvasModel.selectedItem.activity.defineLater = defineLater_chk.selected;
			Debugger.log('_canvasModel.selectedItem.activity.defineLater:'+_canvasModel.selectedItem.activity.defineLater,Debugger.GEN,'click','PropertyInspector');
		
		}else if(tgt.indexOf("runOffline_chk") != -1){
			
			_canvasModel.selectedItem.activity.runOffline = runOffline_chk.selected;
			Debugger.log('_canvasModel.selectedItem.activity.runOffline:'+_canvasModel.selectedItem.activity.runOffline,Debugger.GEN,'click','PropertyInspector');
			_canvasModel.selectedItem.refresh(_canvasModel.selectedItem.activity.runOffline)
		}
		
		setModified();
	}
	
	
	/**
	 * Recieves the click events from the canvas views (inc Property Inspector) buttons.  Based on the label
	 * the relevent method is called to action the user request
	 * @param   evt 
	 */
	public function focusOut(e):Void{
		var tgt:String = new String(e.target);
		Debugger.log('focusOut tgt:'+tgt,Debugger.GEN,'focusOut','PropertyInspector');
		
		if(tgt.indexOf("title_txt") != -1){
			//todo check if this is the right place to set edited content, should it be ddm?
			_canvasModel.selectedItem.activity.title = title_txt.text;
			
		}else if(tgt.indexOf("desc_txt") != -1){
			_canvasModel.selectedItem.activity.description= desc_txt.text;
		}
		_canvasModel.selectedItem.refresh();
		if (_canvasModel.selectedItem.activity.activityTypeID == Activity.PARALLEL_ACTIVITY_TYPE || _canvasModel.selectedItem.activity.activityTypeID == Activity.OPTIONAL_ACTIVITY_TYPE){
			_canvasModel.selectedItem.init();
		}
		
		setModified();
		
	}
	
	/**
	* 
	* Invalidate design (set modified/unsaved flag) when Activity properties are modified
	* 
	*/
	private function setModified():Void{
		_canvasModel.getCanvas().ddm.validDesign = false;
		_canvasModel.getCanvas().checkValidDesign();
		
		ApplicationParent.extCall("setSaved", "false");
	}
	
	private function moveItem(item:Object, xDiff:Number, yDiff:Number) {
		item._x += xDiff;
		item._y += yDiff;
	}
	
}

