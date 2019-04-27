;;; Sierra Script 1.0 - (do not remove this comment)
(script# 996)
(include sci.sh)
(use Main)
(use System)


(instance uEvt of Event
	(properties)
	
	(method (new)
		(= type
			(= message
				(= modifiers (= y (= x (= claimed (= port 0)))))
			)
		)
		(GetEvent evALL_EVENTS self)
		(return self)
	)
)

;	
;	 This class manages user input. The main method of interest here is canControl().
;	
;	 Example usage::
;	
;	 	(if (gUser canControl:)
;	 		; The user is in control...
;	 	)
(class User of Object
	(properties
		alterEgo 0
		input 0
		controls 0
		prevDir 0
		x -1
		y -1
		mapKeyToDir 1
		curEvent 0
	)
	
	(method (init)
		(= curEvent uEvt)
	)
	
	(method (doit)
		(curEvent new:)
		(self handleEvent: curEvent)
	)
	
	;
	; .. function:: canControl()
	;
	; 	:returns: TRUE if the user is currently controlling the game, otherwise FALSE.
	;
	; .. function:: canControl(shouldControl)
	;
	; 	:param boolean shouldControl: TRUE if the user should be in control, otherwise FALSE.
	;
	; 	.. IMPORTANT::
	; 		Generally, (gGame handsOff:) and (gGame handsOn:) should be used to give and take away user control of the game.
	;
	(method (canControl shouldControl)
		(if argc (= controls shouldControl) (= prevDir 0))
		(return controls)
	)
	
	(method (handleEvent pEvent &tmp pEventType pEventMessage pEventModifiers temp3 temp4)
		(= gPEventX (pEvent x?))
		(= gPEventY (pEvent y?))
		(= pEventType (pEvent type?))
		(= pEventModifiers (pEvent modifiers?))
		(if pEventType
			(= gPEvent pEvent)
			(if mapKeyToDir (MapKeyToDir pEvent))
			(if (== pEventType 256)
				(= pEventType 4)
				(= pEventMessage
					(if (& pEventModifiers emSHIFT)
						KEY_ESCAPE
					else
						KEY_RETURN
					)
				)
				(= pEventModifiers 0)
				(pEvent
					type: pEventType
					message: pEventMessage
					modifiers: pEventModifiers
				)
			)
			(if (and gPrints (gPrints handleEvent: pEvent))
				(return 1)
			)
			(pEvent localize:)
			(= pEventType (pEvent type?))
			(= pEventMessage (pEvent message?))
			(cond 
				((& pEventType evSAID)
					(if
						(and
							(== pEventMessage JOY_UP)
							(or
								(= temp4 (gCast firstTrue: #perform findNoun))
								(= temp4 (gFeatures firstTrue: #perform findNoun))
								(= temp4 (gAddToPics firstTrue: #perform findNoun))
							)
						)
						(temp4 doVerb: ((gIconBar curIcon?) message?))
					else
						(= temp4 (gIconBar findIcon: pEventMessage))
						(if temp4
							(gIconBar
								select: (= temp4 (gIconBar findIcon: pEventMessage))
							)
							(gGame setCursor: (temp4 cursor?))
						)
					)
				)
				((& pEventType evJOYSTICK)
					(cond 
						((and gOldDH (gOldDH handleEvent: pEvent)) (return 1))
						(
							(and
								(or
									(and
										gIconBar
										(== (gIconBar curIcon?) (gIconBar walkIconItem?))
									)
									(not gIconBar)
								)
								alterEgo
								controls
								(gCast contains: alterEgo)
								(alterEgo handleEvent: pEvent)
							)
							(return 1)
						)
						(
							(and
								gPseudoMouse
								(or
									(not (& pEventType evKEYBOARD))
									(!= pEventMessage JOY_NULL)
								)
								(gPseudoMouse handleEvent: pEvent)
							)
							(return 1)
						)
					)
				)
				(
					(and
						(& pEventType evKEYBOARD)
						gOldKH
						(gOldKH handleEvent: pEvent)
					)
					(return 1)
				)
				(
					(and
						(& pEventType evMOUSE)
						gOldMH
						(gOldMH handleEvent: pEvent)
					)
					(return 1)
				)
			)
		)
		(if gIconBar (gIconBar handleEvent: pEvent))
		(= pEventType (pEvent type?))
		(= pEventMessage (pEvent message?))
		(if (and input (& pEventType evVERB))
			(cond 
				(
					(and
						(& pEventType evMOVE)
						gWalkHandler
						(gWalkHandler handleEvent: pEvent)
					)
					(return 1)
				)
				(
					(and
						(& pEventType evMOVE)
						(gCast contains: alterEgo)
						controls
						(alterEgo handleEvent: pEvent)
					)
					(return 1)
				)
				(gUseSortedFeatures
					(OnMeAndLowY init:)
					(gCast eachElementDo: #perform OnMeAndLowY pEvent)
					(gFeatures eachElementDo: #perform OnMeAndLowY pEvent)
					(gAddToPics eachElementDo: #perform OnMeAndLowY pEvent)
					(if
						(and
							(OnMeAndLowY theObj?)
							((OnMeAndLowY theObj?) handleEvent: pEvent)
						)
						(return 1)
					)
				)
				((gCast handleEvent: pEvent) (return 1))
				((gFeatures handleEvent: pEvent) (return 1))
				((gAddToPics handleEvent: pEvent) (return 1))
			)
			(if
				(and
					(not (pEvent claimed?))
					(gRegions handleEvent: pEvent)
				)
				(return 1)
			)
		)
		(if pEventType
			(cond 
				((gGame handleEvent: pEvent) (return 1))
				((and gPrints (gPrints handleEvent: pEvent)) (return 1))
			)
		)
		(return 0)
	)
	
	;
	; .. function:: canInput()
	;
	; 	:returns: TRUE if input is enabled, FALSE otherwise.
	;
	; .. function:: canInput(shouldInput)
	;
	; 	:param boolean shouldInput: TRUE if input should be enabled, otherwise FALSE.
	;
	(method (canInput theInput)
		(if argc (= input theInput))
		(return input)
	)
)

;	
;	 This class is helper class used in determining which object the player has clicked on.
(class OnMeAndLowY of Code
	(properties
		theObj 0
		lastY -1
	)
	
	(method (init)
		(= theObj 0)
		(= lastY -1)
	)
	
	(method (doit theTheObj param2)
		(if
		(and (theTheObj onMe: param2) (> (theTheObj y?) lastY))
			(= lastY ((= theObj theTheObj) y?))
		)
	)
)

(instance findNoun of Code
	(properties)
	
	(method (doit param1 param2)
		(return (== (param1 noun?) param2))
	)
)
