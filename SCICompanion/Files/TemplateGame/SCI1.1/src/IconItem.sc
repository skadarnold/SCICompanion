;;; Sierra Script 1.0 - (do not remove this comment)
(script# 937)
(include sci.sh)
(use Main)
(use Print)
(use System)


;	
;	 An icon that represents an action. These are used, for instance, in the main icon bar, or in the inventory dialog.
;	
;	 If you wish to do something in response to the icon being clicked, override the select(params) method in your IconItem instance like so::
;	
;	    (method (select)
;	        (if (super select: &rest)
;	            ; Do something here....
;	        )
;	        return 0
;	    )
(class IconItem of Object
	(properties
		view -1                 ; The view for the icon
		loop -1                 ; The loop for the icon
		cel -1                  ; The cel for the icon
		nsLeft 0
		nsTop -1
		nsRight 0
		nsBottom 0
		state $0000
		cursor -1               ; Cursor number associated with the icon.
		type evVERB
		message -1              ; A verb (e.g. V_LOOK) if type is evVERB.
		modifiers $0000
		signal $0001
		maskView 0
		maskLoop 0
		maskCel 0
		highlightColor 0
		lowlightColor 0
		noun 0                  ; The noun associated with this icon.
		modNum 0
		helpVerb 0
	)
	
	;
	; .. function:: show([left top])
	;
	; 	:param number left: Optional parameter to set left position of icon.
	; 	:param number top: Optional parameter to set top position of icon.
	;
	(method (show theNsLeft theNsTop &tmp [temp0 7])
		(= signal (| signal icVISIBLE))
		(if argc
			(= nsRight
				(+ (= nsLeft theNsLeft) (CelWide view loop cel))
			)
			(= nsBottom
				(+ (= nsTop theNsTop) (CelHigh view loop cel))
			)
		else
			(= nsRight (+ nsLeft (CelWide view loop cel)))
			(= nsBottom (+ nsTop (CelHigh view loop cel)))
		)
		(DrawCel view loop cel nsLeft nsTop -1)
		(if (& signal icDISABLED) (self mask:))
		(if
		(and gPseudoMouse (gPseudoMouse respondsTo: #stop))
			(gPseudoMouse stop:)
		)
	)
	
	;
	; .. function:: select([fProcessEvents])
	;
	; 	:param boolean fProcessEvents: If TRUE, sets its state based on consuming mouse release events. If unspecified, just selects the control.
	; 	:returns: TRUE if the icon was selected, FALSE otherwise.
	;
	(method (select fProcessEvents &tmp newEvent temp1 gGameScript)
		(return
			(cond 
				((& signal icDISABLED) 0)
				((and argc fProcessEvents (& signal notUpd))
					(= temp1 1)
					(DrawCel view loop temp1 nsLeft nsTop -1)
					(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
					(while
					(!= ((= newEvent (Event new:)) type?) evMOUSERELEASE)
						(newEvent localize:)
						(cond 
							((self onMe: newEvent)
								(if (not temp1)
									(= temp1 1)
									(DrawCel view loop temp1 nsLeft nsTop -1)
									(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
								)
							)
							(temp1
								(= temp1 0)
								(DrawCel view loop temp1 nsLeft nsTop -1)
								(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
							)
						)
						(newEvent dispose:)
					)
					(newEvent dispose:)
					(if (== temp1 1)
						(DrawCel view loop 0 nsLeft nsTop -1)
						(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
					)
					(= gGameScript (gGame script?))
					temp1
				)
				(else (= gGameScript (gGame script?)) 1)
			)
		)
	)
	
	(method (highlight param1 &tmp temp0 temp1 temp2 temp3 temp4)
		(if
		(or (not (& signal icVISIBLE)) (== highlightColor -1))
			(return)
		)
		(= temp4
			(if (and argc param1) highlightColor else lowlightColor)
		)
		(= temp0 (+ nsTop 2))
		(= temp1 (+ nsLeft 2))
		(= temp2 (- nsBottom 3))
		(= temp3 (- nsRight 4))
		(Graph grDRAW_LINE temp0 temp1 temp0 temp3 temp4 -1 -1)
		(Graph grDRAW_LINE temp0 temp3 temp2 temp3 temp4 -1 -1)
		(Graph grDRAW_LINE temp2 temp3 temp2 temp1 temp4 -1 -1)
		(Graph grDRAW_LINE temp2 temp1 temp0 temp1 temp4 -1 -1)
		(Graph
			grUPDATE_BOX
			(- nsTop 2)
			(- nsLeft 2)
			nsBottom
			(+ nsRight 3)
			1
		)
	)
	
	(method (onMe param1)
		(return
			(if
				(and
					(>= (param1 x?) nsLeft)
					(>= (param1 y?) nsTop)
					(<= (param1 x?) nsRight)
				)
				(<= (param1 y?) nsBottom)
			else
				0
			)
		)
	)
	
	(method (mask)
		(DrawCel
			maskView
			maskLoop
			maskCel
			(+
				nsLeft
				(/
					(-
						(CelWide view loop cel)
						(CelWide maskView maskLoop maskCel)
					)
					2
				)
			)
			(+
				nsTop
				(/
					(-
						(CelHigh view loop cel)
						(CelHigh maskView maskLoop maskCel)
					)
					2
				)
			)
			-1
		)
	)
)

;	
;	 A class that manages the icon bar at the top of the screen in the template game.
(class IconBar of Set
	(properties
		elements 0
		size 0
		height 0
		underBits 0
		oldMouseX 0
		oldMouseY 0
		curIcon 0
		highlightedIcon 0
		prevIcon 0
		curInvIcon 0
		useIconItem 0
		helpIconItem 0
		walkIconItem 0
		port 0
		window 0
		state $0400
		activateHeight 0
		y 0
	)
	
	(method (doit &tmp temp0 temp1 temp2 temp3 gGameScript)
		(while
			(and
				(& state $0020)
				(= temp0 ((gUser curEvent?) new:))
			)
			(= temp1 (temp0 type?))
			(= temp2 (temp0 message?))
			(= temp3 (temp0 modifiers?))
			(Wait 1)
			(= gGameTime (+ gTickOffset (GetTime)))
			(if gCuees (gCuees eachElementDo: #doit))
			(= gGameScript (gGame script?))
			(if (== temp1 256)
				(= temp1 evKEYBOARD)
				(= temp2
					(if (& temp3 emSHIFT) KEY_ESCAPE else KEY_RETURN)
				)
				(= temp3 0)
				(temp0 type: temp1 message: temp2 modifiers: temp3)
			)
			(temp0 localize:)
			(if
				(and
					(or
						(== temp1 evMOUSEBUTTON)
						(and (== temp1 evKEYBOARD) (== temp2 KEY_RETURN))
					)
					(IsObject helpIconItem)
					(& (helpIconItem signal?) $0010)
				)
				(temp0 type: evHELPVERB message: (helpIconItem message?))
			)
			(MapKeyToDir temp0)
			(breakif (self dispatchEvent: temp0))
		)
	)
	
	(method (handleEvent pEvent &tmp temp0 pEventType temp2 temp3 theGCursorNumber theCurIcon theCurInvIcon)
		(pEvent localize:)
		(= pEventType (pEvent type?))
		(cond 
			((& state $0004))
			(
				(or
					(and
						(not pEventType)
						(& state $0400)
						(<= -10 (pEvent y?))
						(<= (pEvent y?) height)
						(<= 0 (pEvent x?))
						(<= (pEvent x?) 320)
						(not (= temp0 0))
					)
					(and
						(== pEventType evKEYBOARD)
						(or
							(== (pEvent message?) KEY_ESCAPE)
							(== (pEvent message?) KEY_DELETE)
						)
						(= temp0 1)
					)
				)
				(pEvent globalize:)
				(= oldMouseX (pEvent x?))
				(= oldMouseY (pEvent y?))
				(= theGCursorNumber gCursorNumber)
				(= theCurIcon curIcon)
				(= theCurInvIcon curInvIcon)
				(self show:)
				(gGame setCursor: 999)
				(if temp0
					(gGame
						setCursor:
							gCursorNumber
							1
							(+
								(curIcon nsLeft?)
								(/ (- (curIcon nsRight?) (curIcon nsLeft?)) 2)
							)
							(- (curIcon nsBottom?) 3)
					)
				)
				(self doit:)
				(= temp3
					(if (or (gUser canControl:) (gUser canInput:))
						(curIcon cursor?)
					else
						gWaitCursor
					)
				)
				(if temp0
					(gGame setCursor: temp3 1 oldMouseX oldMouseY)
				else
					(gGame
						setCursor: temp3 1 ((pEvent new:) x?) (Max (pEvent y?) (+ 1 height))
					)
				)
				(self hide:)
			)
			((& pEventType evKEYBOARD)
				(switch (pEvent message?)
					(KEY_RETURN
						(cond 
							((not (IsObject curIcon)))
							((or (!= curIcon useIconItem) curInvIcon)
								(pEvent
									type: (curIcon type?)
									message:
										(if (== curIcon useIconItem)
											(curInvIcon message?)
										else
											(curIcon message?)
										)
								)
							)
							(else (pEvent type: 0))
						)
					)
					(KEY_NUMPAD0
						(if (gUser canControl:) (self swapCurIcon:))
						(pEvent claimed: TRUE)
					)
					(JOY_NULL
						(if (& (pEvent type?) evJOYSTICK)
							(self advanceCurIcon:)
							(pEvent claimed: TRUE)
						)
					)
				)
			)
			((& pEventType evMOUSEBUTTON)
				(cond 
					((& (pEvent modifiers?) emSHIFT) (self advanceCurIcon:) (pEvent claimed: TRUE))
					((& (pEvent modifiers?) emCTRL)
						(if (gUser canControl:) (self swapCurIcon:))
						(pEvent claimed: TRUE)
					)
					((IsObject curIcon)
						(pEvent
							type: (curIcon type?)
							message:
								(if (== curIcon useIconItem)
									(curInvIcon message?)
								else
									(curIcon message?)
								)
						)
					)
				)
			)
		)
	)
	
	(method (show &tmp temp0 temp1 temp2 temp3 theY temp5 temp6 temp7)
		(gSounds pause:)
		(= state (| state $0020))
		(gGame setCursor: 999 1)
		(= temp0 (self at: 0))
		(= height
			(CelHigh (temp0 view?) (temp0 loop?) (temp0 cel?))
		)
		(= port (GetPort))
		(SetPort -1)
		(= underBits (Graph grSAVE_BOX y 0 (+ y height) 320 1))
		(= temp1 (PicNotValid))
		(PicNotValid 1)
		(= temp3 0)
		(= theY y)
		(= temp5 (FirstNode elements))
		(while temp5
			(= temp6 (NextNode temp5))
			(= temp7 (NodeValue temp5))
			(if (not (IsObject temp7)) (return))
			(if (<= (temp7 nsRight?) 0)
				(temp7 show: temp3 theY)
				(= temp3 (temp7 nsRight?))
			else
				(temp7 show:)
			)
			(= temp5 temp6)
		)
		(if curInvIcon
			(if (gEgo has: (gInv indexOf: curInvIcon))
				(= temp3
					(+
						(/
							(-
								(- (useIconItem nsRight?) (useIconItem nsLeft?))
								(CelWide
									(curInvIcon view?)
									(curInvIcon loop?)
									(curInvIcon cel?)
								)
							)
							2
						)
						(useIconItem nsLeft?)
					)
				)
				(= theY
					(+
						y
						(/
							(-
								(- (useIconItem nsBottom?) (useIconItem nsTop?))
								(CelHigh
									(curInvIcon view?)
									(curInvIcon loop?)
									(curInvIcon cel?)
								)
							)
							2
						)
						(useIconItem nsTop?)
					)
				)
				(DrawCel
					(curInvIcon view?)
					(curInvIcon loop?)
					(curInvIcon cel?)
					temp3
					theY
					-1
				)
				(if (& (useIconItem signal?) icDISABLED)
					(useIconItem mask:)
				)
			else
				(= curInvIcon 0)
			)
		)
		(PicNotValid temp1)
		(Graph grUPDATE_BOX y 0 (+ y height) 320 1)
		(self highlight: curIcon)
	)
	
	(method (hide &tmp temp0 temp1 temp2)
		(if (& state $0020)
			(gSounds pause: FALSE)
			(= state (& state $ffdf))
			(= temp0 (FirstNode elements))
			(while temp0
				(= temp1 (NextNode temp0))
				(= temp2 (NodeValue temp0))
				(if (not (IsObject temp2)) (return))
				(= temp2 (NodeValue temp0))
				(temp2 signal: (& (temp2 signal?) (~ icVISIBLE)))
				(= temp0 temp1)
			)
			(if
				(and
					(not (& state $0800))
					(IsObject helpIconItem)
					(& (helpIconItem signal?) $0010)
				)
				(helpIconItem signal: (& (helpIconItem signal?) $ffef))
			)
			(Graph grRESTORE_BOX underBits)
			(Graph grUPDATE_BOX y 0 (+ y height) 320 1)
			(Graph grREDRAW_BOX y 0 (+ y height) 320)
			(SetPort port)
			(= height activateHeight)
		)
	)
	
	(method (advance &tmp temp0 temp1)
		(= temp1 1)
		(while (<= temp1 size)
			(= temp0
				(self
					at: (mod (+ temp1 (self indexOf: highlightedIcon)) size)
				)
			)
			(if (not (IsObject temp0))
				(= temp0 (NodeValue (self first:)))
			)
			(breakif (not (& (temp0 signal?) icDISABLED)))
			(= temp1 (mod (+ temp1 1) size))
		)
		(self highlight: temp0 (& state $0020))
	)
	
	(method (retreat &tmp temp0 temp1)
		(= temp1 1)
		(while (<= temp1 size)
			(= temp0
				(self
					at: (mod (- (self indexOf: highlightedIcon) temp1) size)
				)
			)
			(if (not (IsObject temp0))
				(= temp0 (NodeValue (self last:)))
			)
			(breakif (not (& (temp0 signal?) icDISABLED)))
			(= temp1 (mod (+ temp1 1) size))
		)
		(self highlight: temp0 (& state $0020))
	)
	
	;
	; .. function:: select(theCurIcon [fProcessEvents])
	;
	(method (select theCurIcon fProcessEvents)
		(return
			(if
			(theCurIcon select: (if (>= argc 2) fProcessEvents))
				(if (not (& (theCurIcon signal?) $0002))
					(= curIcon theCurIcon)
				)
				1
			else
				0
			)
		)
	)
	
	(method (highlight theHighlightedIcon param2 &tmp temp0)
		(if
		(not (& (theHighlightedIcon signal?) icDISABLED))
			(if (IsObject highlightedIcon)
				(highlightedIcon highlight: 0)
			)
			((= highlightedIcon theHighlightedIcon) highlight: 1)
		)
		(if (and (>= argc 2) param2)
			(gGame
				setCursor:
					gCursorNumber
					1
					(+
						(theHighlightedIcon nsLeft?)
						(/
							(-
								(theHighlightedIcon nsRight?)
								(theHighlightedIcon nsLeft?)
							)
							2
						)
					)
					(- (theHighlightedIcon nsBottom?) 3)
			)
		)
	)
	
	(method (swapCurIcon &tmp temp0)
		(if (& state icDISABLED)
			(return)
		else
			(= temp0 (NodeValue (self first:)))
			(cond 
				(
					(and
						(!= curIcon temp0)
						(not (& (temp0 signal?) icDISABLED))
					)
					(= prevIcon curIcon)
					(= curIcon (NodeValue (self first:)))
				)
				(
				(and prevIcon (not (& (prevIcon signal?) icDISABLED))) (= curIcon prevIcon))
			)
		)
		(gGame setCursor: (curIcon cursor?) 1)
	)
	
	(method (advanceCurIcon &tmp theCurIcon temp1 temp2)
		(if (& state $0004) (return))
		(= theCurIcon curIcon)
		(= temp1 0)
		(while
			(&
				((= theCurIcon
					(self at: (mod (+ (self indexOf: theCurIcon) 1) size))
				)
					signal?
				)
				$0006
			)
			(if (> temp1 (+ 1 size)) (return) else (++ temp1))
		)
		(= curIcon theCurIcon)
		(gGame setCursor: (curIcon cursor?) 1)
	)
	
	(method (dispatchEvent pEvent &tmp temp0 temp1 eventType eventMessage theHighlightedIcon temp5 temp6 [temp7 50] temp57 theHighlightedIconSignal temp59 temp60)
		(= temp1 (pEvent x?))
		(= temp0 (pEvent y?))
		(= eventType (pEvent type?))
		(= eventMessage (pEvent message?))
		(= temp5 (pEvent claimed?))
		(= theHighlightedIcon (self firstTrue: #onMe pEvent))
		(if theHighlightedIcon
			(= temp57
				((= theHighlightedIcon (self firstTrue: #onMe pEvent))
					cursor?
				)
			)
			(= theHighlightedIconSignal (theHighlightedIcon signal?))
			(= temp59 (== theHighlightedIcon helpIconItem))
		)
		(if (& eventType evJOYSTICK)
			(switch eventMessage
				(JOY_RIGHT (self advance:))
				(JOY_LEFT (self retreat:))
			)
		else
			(switch eventType
				(evNULL
					(cond 
						(
							(not
								(if
									(and
										(<= 0 temp0)
										(<= temp0 (+ y height))
										(<= 0 temp1)
									)
									(<= temp1 320)
								)
							)
							(if
								(and
									(& state $0400)
									(or
										(not (IsObject helpIconItem))
										(not (& (helpIconItem signal?) $0010))
									)
								)
								(= oldMouseY 0)
								(= temp5 1)
							)
						)
						(
							(and
								theHighlightedIcon
								(!= theHighlightedIcon highlightedIcon)
							)
							(= oldMouseY 0)
							(self highlight: theHighlightedIcon)
						)
					)
				)
				(evMOUSEBUTTON
					(if
						(and
							theHighlightedIcon
							(self select: theHighlightedIcon TRUE)
						)
						(if temp59
							(if temp57 (gGame setCursor: temp57))
							(if (& state $0800)
								(self noClickHelp:)
							else
								(helpIconItem signal: (| (helpIconItem signal?) $0010))
							)
						else
							(= temp5 (& theHighlightedIconSignal $0040))
						)
						(theHighlightedIcon doit:)
					)
				)
				(evKEYBOARD
					(switch eventMessage
						(KEY_ESC (= temp5 1))
						(KEY_DELETE (= temp5 1))
						(KEY_RETURN
							(if (not theHighlightedIcon)
								(= theHighlightedIcon highlightedIcon)
							)
							(cond 
								(
									(and
										theHighlightedIcon
										(== theHighlightedIcon helpIconItem)
									)
									(if (!= temp57 -1) (gGame setCursor: temp57))
									(if helpIconItem
										(helpIconItem signal: (| (helpIconItem signal?) $0010))
									)
								)
								(
									(and
										(IsObject theHighlightedIcon)
										(self select: theHighlightedIcon)
									)
									(theHighlightedIcon doit:)
									(= temp5 (& theHighlightedIconSignal $0040))
								)
							)
						)
						(KEY_SHIFTTAB (self retreat:))
						(KEY_TAB (self advance:))
					)
				)
				(evHELPVERB
					(if
					(and theHighlightedIcon (theHighlightedIcon helpVerb?))
						(if (not (HaveMouse))
							(= temp60 (gGame setCursor: 996))
						)
						(= temp6 (GetPort))
						(Print
							font: gFont
							width: 250
							addText:
								(theHighlightedIcon noun?)
								(theHighlightedIcon helpVerb?)
								0
								1
								0
								0
								(theHighlightedIcon modNum?)
							init:
						)
						(SetPort temp6)
						(if (not (HaveMouse)) (gGame setCursor: temp60))
					)
					(if helpIconItem
						(helpIconItem signal: (& (helpIconItem signal?) $ffef))
					)
					(gGame setCursor: 999)
				)
			)
		)
		(return temp5)
	)
	
	(method (disable param1 &tmp temp0 temp1)
		(if argc
			(= temp0 0)
			(while (< temp0 argc)
				(= temp1
					(if (IsObject [param1 temp0])
						[param1
						temp0]
					else
						(self at: [param1 temp0])
					)
				)
				(temp1 signal: (| (temp1 signal?) icDISABLED))
				(cond 
					((== temp1 curIcon) (self advanceCurIcon:))
					((== temp1 highlightedIcon) (self advance:))
				)
				(++ temp0)
			)
		else
			(= state (| state $0004))
		)
	)
	
	(method (enable param1 &tmp temp0 temp1)
		(if argc
			(= temp0 0)
			(while (< temp0 argc)
				(= temp1
					(if (IsObject [param1 temp0])
						[param1
						temp0]
					else
						(self at: [param1 temp0])
					)
				)
				(temp1 signal: (& (temp1 signal?) (~ icDISABLED)))
				(++ temp0)
			)
		else
			(= state (& state $fffb))
		)
	)
	
	(method (noClickHelp &tmp temp0 temp1 temp2 temp3 winEraseOnly temp5)
		(= temp2 0)
		(= temp1 temp2)
		(= temp3 (GetPort))
		(= winEraseOnly (gWindow eraseOnly?))
		(gWindow eraseOnly: 1)
		(while
		(not ((= temp0 ((gUser curEvent?) new:)) type?))
			(if (not (self isMemberOf: IconBar)) (temp0 localize:))
			(= temp2 (self firstTrue: #onMe temp0))
			(cond 
				(temp2
					(if
						(and
							(!= (= temp2 (self firstTrue: #onMe temp0)) temp1)
							(temp2 helpVerb?)
						)
						(= temp1 temp2)
						(if gDialog (gDialog dispose:))
						(Print
							font: gFont
							width: 250
							addText: (temp2 noun?) (temp2 helpVerb?) 0 1 0 0 (temp2 modNum?)
							modeless: 1
							init:
						)
						(SetPort temp3)
					)
				)
				(gDialog (gDialog dispose:))
				(else (= temp1 0))
			)
			(temp0 dispose:)
		)
		(gWindow eraseOnly: winEraseOnly)
		(gGame setCursor: 999 1)
		(if gDialog (gDialog dispose:))
		(SetPort temp3)
		(if (not (helpIconItem onMe: temp0))
			(self dispatchEvent: temp0)
		)
	)
	
	(method (findIcon param1 &tmp temp0 temp1)
		(= temp0 0)
		(while (< temp0 size)
			(= temp1 (self at: temp0))
			(if (== (temp1 message?) param1) (return temp1))
			(++ temp0)
		)
		(return 0)
	)
)
