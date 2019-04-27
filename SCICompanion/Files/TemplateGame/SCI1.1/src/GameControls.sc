;;; Sierra Script 1.0 - (do not remove this comment)
(script# CONTROLSBASE_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use Print)
(use IconItem)


;	
;	 This is a base class for the settings dialog.
(class GameControls of IconBar
	(properties
		elements 0
		size 0
		height 200
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
		state $0000
		activateHeight 0
		y 0
		okButton 0
	)
	
	(method (show &tmp temp0 temp1 temp2 temp3 temp4)
		(gSounds pause:)
		(if
		(and gPseudoMouse (gPseudoMouse respondsTo: #stop))
			(gPseudoMouse stop:)
		)
		(= state (| state $0020))
		(if (IsObject window)
			(window open:)
		else
			(= window
				((gWindow new:)
					top: 46
					left: 24
					bottom: 155
					right: 296
					priority: 15
					open:
					yourself:
				)
			)
		)
		(= temp0 30)
		(= temp1 30)
		(= temp2 (FirstNode elements))
		(while temp2
			(= temp3 (NextNode temp2))
			(= temp4 (NodeValue temp2))
			(if (not (IsObject temp4)) (return))
			(if
				(and
					(not (& (temp4 signal?) $0080))
					(<= (temp4 nsRight?) 0)
				)
				(temp4 show: temp0 temp1)
				(= temp0 (+ 20 (temp4 nsRight?)))
			else
				(temp4 show:)
			)
			(= temp2 temp3)
		)
		(if (not okButton)
			(= okButton (NodeValue (self first:)))
		)
		(if curIcon
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
		(self doit: hide:)
	)
	
	(method (hide)
		(if window (window dispose:) (= window 0))
		(if (& state $0020)
			(gSounds pause: FALSE)
			(= state (& state $ffdf))
		)
	)
	
	(method (select theControl fSelect)
		(theControl select: (if (>= argc 2) fSelect else 0))
	)
	
	(method (swapCurIcon)
	)
	
	(method (advanceCurIcon &tmp temp0)
	)
	
	(method (dispatchEvent param1 &tmp winEraseOnly temp1 temp2 [temp3 50] temp53 temp54)
		(= temp53 (param1 type?))
		(= temp54 (param1 message?))
		(return
			(cond 
				((== temp53 8192)
					(= temp1 (self firstTrue: #onMe param1))
					(if
						(and
							temp1
							((= temp1 (self firstTrue: #onMe param1)) helpVerb?)
						)
						(= temp2 (GetPort))
						(if (gWindow respondsTo: #eraseOnly)
							(= winEraseOnly (gWindow eraseOnly?))
							(gWindow eraseOnly: 1)
							(Print
								font: gFont
								width: 250
								addText: (temp1 noun?) (temp1 helpVerb?) 0 1 0 0 (temp1 modNum?)
								init:
							)
							(gWindow eraseOnly: winEraseOnly)
						else
							(Print
								font: gFont
								width: 250
								addText: (temp1 noun?) (temp1 helpVerb?) 0 1 0 0 (temp1 modNum?)
								init:
							)
						)
						(SetPort temp2)
					)
					(if helpIconItem
						(helpIconItem signal: (& (helpIconItem signal?) $ffef))
					)
					(gGame setCursor: 999)
					(return 0)
				)
				((& temp53 $0040)
					(switch temp54
						(5
							(cond 
								(
									(and
										(IsObject highlightedIcon)
										(highlightedIcon respondsTo: #retreat)
									)
									(highlightedIcon retreat:)
									(return 0)
								)
								(
									(or
										(not (IsObject highlightedIcon))
										(& (highlightedIcon signal?) $0100)
									)
									(self advance:)
									(return 0)
								)
							)
						)
						(1
							(cond 
								(
									(and
										(IsObject highlightedIcon)
										(highlightedIcon respondsTo: #advance)
									)
									(highlightedIcon advance:)
									(return 0)
								)
								(
									(or
										(not (IsObject highlightedIcon))
										(& (highlightedIcon signal?) $0100)
									)
									(self retreat:)
									(return 0)
								)
							)
						)
						(else 
							(super dispatchEvent: param1)
						)
					)
				)
				(else (super dispatchEvent: param1))
			)
		)
	)
)

;	
;	 Extends :class:`IconItem` by having a object and selector.
;	
;	 When clicked (selected), the *selector* property will be invoked on *theObj*.
;	
;	 Example::
;	
;	 	(instance iconRestart of ControlIcon
;	 		(properties
;	 			view 995
;	 			loop 4
;	 			cel 0
;	 			nsLeft 80
;	 			nsTop 82
;	 			message 8
;	 			signal $01c3
;	 			noun N_RESTART
;	 			helpVerb V_HELP
;	 		)
;	 	)
;	
;	 	; then later...
;	
;	 	(iconRestart
;	 		theObj: gGame
;	 		selector: #restart
;	 	)
;	
(class ControlIcon of IconItem
	(properties
		view -1
		loop -1
		cel -1
		nsLeft 0
		nsTop -1
		nsRight 0
		nsBottom 0
		state $0000
		cursor -1
		type evVERB
		message -1
		modifiers $0000
		signal $0001
		maskView 0
		maskLoop 0
		maskCel 0
		highlightColor 0
		lowlightColor 0
		noun 0
		modNum 0
		helpVerb 0
		theObj 0                ; An object that gets notified when this icon is pressed.
		selector 0              ; A method selector (e.g. #doit) on theObj.
	)
	
	(method (doit)
		(if theObj
			(if (& signal $0040)
				((if gGameControls else GameControls) hide:)
			)
			(gGame panelObj: theObj panelSelector: selector)
		)
	)
	
	(method (select)
		(super select: &rest)
		(self doit:)
	)
)
