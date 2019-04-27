;;; Sierra Script 1.0 - (do not remove this comment)
(script# 933)
(include sci.sh)
(use Main)
(use System)


;	
;	 Code that lets the player move the mouse cursor with the joystick or keyboard.
(class PseudoMouse of Code
	(properties
		cursorInc 2
		minInc 2
		maxInc 20
		prevDir 0
		joyInc 5
	)
	
	(method (doit &tmp gPEventX gPEventY)
		(= gPEventX (gPEvent x?))
		(= gPEventY (gPEvent y?))
		(switch prevDir
			(1
				(= gPEventY (- gPEventY cursorInc))
			)
			(2
				(= gPEventX (+ gPEventX cursorInc))
				(= gPEventY (- gPEventY cursorInc))
			)
			(3
				(= gPEventX (+ gPEventX cursorInc))
			)
			(4
				(= gPEventX (+ gPEventX cursorInc))
				(= gPEventY (+ gPEventY cursorInc))
			)
			(5
				(= gPEventY (+ gPEventY cursorInc))
			)
			(6
				(= gPEventX (- gPEventX cursorInc))
				(= gPEventY (+ gPEventY cursorInc))
			)
			(7
				(= gPEventX (- gPEventX cursorInc))
			)
			(8
				(= gPEventX (- gPEventX cursorInc))
				(= gPEventY (- gPEventY cursorInc))
			)
		)
		(gGame setCursor: gCursorNumber 1 gPEventX gPEventY)
	)
	
	(method (handleEvent pEvent &tmp pEventType thePrevDir pEventModifiers)
		(= pEventType (pEvent type?))
		(= thePrevDir (pEvent message?))
		(= pEventModifiers (pEvent modifiers?))
		(if (& pEventType evJOYSTICK)
			(= prevDir thePrevDir)
			(= cursorInc
				(if (& pEventType evKEYBOARD)
					(if (& pEventModifiers emSHIFT) minInc else maxInc)
				else
					joyInc
				)
			)
			(cond 
				((& pEventType evKEYBOARD)
					(if prevDir
						(self doit:)
					else
						(pEvent claimed: FALSE)
						(return)
					)
				)
				(prevDir (self start:))
				(else (self stop:))
			)
			(pEvent claimed: TRUE)
			(return)
		)
	)
	
	(method (start thePrevDir)
		(if argc (= prevDir thePrevDir))
		(gTheDoits add: self)
	)
	
	(method (stop)
		(= prevDir 0)
		(gTheDoits delete: self)
	)
)
